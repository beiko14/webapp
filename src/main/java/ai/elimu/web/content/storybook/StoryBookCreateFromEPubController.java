package ai.elimu.web.content.storybook;

import ai.elimu.dao.StoryBookChapterDao;
import ai.elimu.dao.StoryBookDao;
import ai.elimu.dao.StoryBookParagraphDao;
import ai.elimu.dao.WordDao;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import ai.elimu.model.content.StoryBook;
import ai.elimu.model.content.StoryBookChapter;
import ai.elimu.model.content.StoryBookParagraph;
import ai.elimu.model.content.Word;
import ai.elimu.model.enums.Language;
import ai.elimu.util.ConfigHelper;
import ai.elimu.util.WordExtractionHelper;
import ai.elimu.util.epub.EPubChapterExtractionHelper;
import ai.elimu.util.epub.EPubMetadataExtractionHelper;
import ai.elimu.util.epub.EPubParagraphExtractionHelper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

@Controller
@RequestMapping("/content/storybook/create-from-epub")
public class StoryBookCreateFromEPubController {
    
    private final Logger logger = Logger.getLogger(getClass());
    
    @Autowired
    private StoryBookDao storyBookDao;
    
    @Autowired
    private StoryBookChapterDao storyBookChapterDao;
    
    @Autowired
    private StoryBookParagraphDao storyBookParagraphDao;
    
    @Autowired
    private WordDao wordDao;

    @RequestMapping(method = RequestMethod.GET)
    public String handleRequest(Model model) {
    	logger.info("handleRequest");
        
        StoryBook storyBook = new StoryBook();
        model.addAttribute("storyBook", storyBook);

        return "content/storybook/create-from-epub";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String handleSubmit(
            StoryBook storyBook,
            @RequestParam("bytes") MultipartFile multipartFile,
            BindingResult result,
            Model model,
            HttpSession session
    ) {
    	logger.info("handleSubmit");
        
        List<StoryBookChapter> storyBookChapters = new ArrayList<>();
        
        List<StoryBookParagraph> storyBookParagraphs = new ArrayList<>();
        
        Language language = Language.valueOf(ConfigHelper.getProperty("content.language"));
        storyBook.setLanguage(language);
        
        if (multipartFile.isEmpty()) {
            result.rejectValue("bytes", "NotNull");
        } else {
            String contentType = multipartFile.getContentType();
            logger.info("contentType: " + contentType);
            
            String name = multipartFile.getName();
            logger.info("name: " + name);
            
            String originalFilename = multipartFile.getOriginalFilename();
            logger.info("originalFilename: " + originalFilename);
            
            long size = multipartFile.getSize();
            logger.info("size: " + size + " (" + (size / 1024) + "kB)");
            
            try {    
                byte[] bytes = multipartFile.getBytes();
                logger.info("bytes.length: " + (bytes.length / 1024 / 1024) + "MB");
                 
                List<File> filesInEPub = unzipFiles(bytes, originalFilename);
                logger.info("filesInEPub.size(): " + filesInEPub.size());
                
                // Extract the ePUB's metadata from its OPF file
                File opfFile = null;
                for (File file : filesInEPub) {
                    if (file.getName().endsWith(".opf")) {
                        opfFile = file;
                    }
                }
                logger.info("opfFile: \"" + opfFile + "\"");
                if (opfFile == null) {
                    throw new FileNotFoundException("The OPF file was not found");
                } else {
                    String title = EPubMetadataExtractionHelper.extractTitleFromOpfFile(opfFile);
                    logger.info("title: \"" + title + "\"");
                    storyBook.setTitle(title);
                    
                    String description = EPubMetadataExtractionHelper.extractDescriptionFromOpfFile(opfFile);
                    logger.info("description: \"" + description + "\"");
                    storyBook.setDescription(description);
                }
                
                // Extract the ePUB's chapters
                File tableOfContentsFile = null;
                for (File file : filesInEPub) {
                    if ("toc.xhtml".equals(file.getName())) {
                        tableOfContentsFile = file;
                    }
                }
                logger.info("tableOfContentsFile: \"" + tableOfContentsFile + "\"");
                if (tableOfContentsFile == null) {
                    throw new FileNotFoundException("The TOC file was not found");
                } else {
                    List<String> chapterReferences = EPubChapterExtractionHelper.extractChapterReferencesFromTableOfContentsFile(tableOfContentsFile);
                    logger.info("chapterReferences.size(): " + chapterReferences.size());
                    
                    // Extract each chapter's paragraphs
                    for (String chapterReference : chapterReferences) {
                        logger.info("chapterReference: \"" + chapterReference + "\"");
                        File chapterFile = new File(opfFile.getParent(), chapterReference);
                        logger.info("chapterFile: \"" + chapterFile + "\"");
                        StoryBookChapter storyBookChapter = new StoryBookChapter();
                        storyBookChapter.setSortOrder(storyBookChapters.size());
                        storyBookChapters.add(storyBookChapter);

                        List<String> paragraphs = EPubParagraphExtractionHelper.extractParagraphsFromChapterFile(chapterFile);
                        logger.info("paragraphs.size(): " + paragraphs.size());
                        for (int i = 0; i < paragraphs.size(); i++) {
                            String paragraph = paragraphs.get(i);
                            logger.info("paragraph: \"" + paragraph + "\"");
                            logger.info("paragraph.length(): " + paragraph.length());
                            
                            StoryBookParagraph storyBookParagraph = new StoryBookParagraph();
                            storyBookParagraph.setStoryBookChapter(storyBookChapter);
                            storyBookParagraph.setSortOrder(i);
                            
                            if (paragraph.length() > 1024) {
                                logger.warn("Reducing the length of the paragraph to its initial 1,024 characters.");
                                paragraph = paragraph.substring(0, 1023);
                            }
                            storyBookParagraph.setOriginalText(paragraph);
                            
                            List<String> wordsInOriginalText = WordExtractionHelper.getWords(storyBookParagraph.getOriginalText());
                            logger.info("wordsInOriginalText.size(): " + wordsInOriginalText.size());
                            List<Word> words = new ArrayList<>();
                            logger.info("words.size(): " + words.size());
                            for (String wordInOriginalText : wordsInOriginalText) {
                                logger.info("wordInOriginalText: \"" + wordInOriginalText + "\"");
                                Word word = wordDao.readByText(language, wordInOriginalText);
                                logger.info("word: " + word);
                                words.add(word);
                            }
                            storyBookParagraph.setWords(words);
                            
                            storyBookParagraphs.add(storyBookParagraph);
                        }
                    }
                }
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
        
        if (result.hasErrors()) {
            return "content/storybook/create-from-epub";
        } else {
            // Store the StoryBook in the database
            storyBook.setTimeLastUpdate(Calendar.getInstance());
            storyBookDao.create(storyBook);
            
            // Store the StoryBookChapters in the database
            for (StoryBookChapter storyBookChapter : storyBookChapters) {
                storyBookChapter.setStoryBook(storyBook);
                storyBookChapterDao.create(storyBookChapter);
                
                // Store the StoryBookChapter's StoryBookParagraphs in the database
                for (StoryBookParagraph storyBookParagraph : storyBookParagraphs) {
                    if (storyBookParagraph.getStoryBookChapter().getSortOrder() == storyBookChapter.getSortOrder()) {
                        storyBookParagraph.setStoryBookChapter(storyBookChapter);
                        storyBookParagraphDao.create(storyBookParagraph);
                    }
                }
            }
            
            return "redirect:/content/storybook/edit/" + storyBook.getId();
        }
    }
    
    /**
     * See http://www.mkyong.com/spring-mvc/spring-mvc-failed-to-convert-property-value-in-file-upload-form/
     * <p></p>
     * Fixes this error message:
     * "Cannot convert value of type [org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile] to required type [byte] for property 'bytes[0]'"
     */
    @InitBinder
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
    	logger.info("initBinder");
    	binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
    }
    
    /**
     * Unzip the contents of the ePUB file to a temporary folder.
     */
    private List<File> unzipFiles(byte[] bytes, String originalFilename) {
        logger.info("unzipFiles");
        
        List<File> unzippedFiles = new ArrayList<>();
        
        String tmpDir = System.getProperty("java.io.tmpdir");
        logger.info("tmpDir: " + tmpDir);
        File tmpDirElimuAi = new File(tmpDir, "elimu-ai");
        logger.info("tmpDirElimuAi: " + tmpDirElimuAi);
        logger.info("tmpDirElimuAi.mkdir(): " + tmpDirElimuAi.mkdir());
        File unzipDestinationDirectory = new File(tmpDirElimuAi, originalFilename.replace(" ", "_") + "_unzipped");
        logger.info("unzipDestinationDirectory: " + unzipDestinationDirectory);
        logger.info("unzipDestinationDirectory.mkdir(): " + unzipDestinationDirectory.mkdir());
        byte[] buffer = new byte[1024];
        try {
            InputStream inputStream = new ByteArrayInputStream(bytes);
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                logger.info("zipEntry: " + zipEntry);
                
                // Create intermediate folders.
                File metaInfDirectory = new File(unzipDestinationDirectory, "META-INF");
                logger.info("metaInfDirectory.mkdir(): " + metaInfDirectory.mkdir());
                File contentDirectory = new File(unzipDestinationDirectory, "content");
                logger.info("contentDirectory.mkdir(): " + contentDirectory.mkdir());
                
                // E.g. unzipDestinationDirectory + "/" + "META-INF/container.xml"
                File unzipDestinationFile = new File(unzipDestinationDirectory + File.separator + zipEntry.toString());
                logger.info("unzipDestinationFile: " + unzipDestinationFile);
                
                // Write file to disk
                FileOutputStream fileOutputStream = new FileOutputStream(unzipDestinationFile);
                int length;
                while ((length = zipInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }
                fileOutputStream.close();
                
                logger.info("unzipDestinationFile.exists(): " + unzipDestinationFile.exists());
                unzippedFiles.add(unzipDestinationFile);
            }
            zipInputStream.close();
            inputStream.close();
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        } catch (IOException ex) {
            logger.error(null, ex);
        }
        
        return unzippedFiles;
    }
}
