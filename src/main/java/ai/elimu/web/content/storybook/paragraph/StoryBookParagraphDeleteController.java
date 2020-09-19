package ai.elimu.web.content.storybook.paragraph;

import ai.elimu.dao.StoryBookContributionEventDao;
import ai.elimu.dao.StoryBookDao;
import ai.elimu.dao.StoryBookParagraphDao;
import ai.elimu.model.content.StoryBook;
import ai.elimu.model.content.StoryBookParagraph;
import org.apache.logging.log4j.Logger;
import ai.elimu.model.contributor.Contributor;
import ai.elimu.model.contributor.StoryBookContributionEvent;
import ai.elimu.model.enums.Role;
import ai.elimu.rest.v2.service.StoryBooksJsonService;
import java.util.Calendar;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/content/storybook/paragraph/delete")
public class StoryBookParagraphDeleteController {
    
    private final Logger logger = Logger.getLogger(getClass());
    
    @Autowired
    private StoryBookDao storyBookDao;
    
    @Autowired
    private StoryBookContributionEventDao storyBookContributionEventDao;
    
    @Autowired
    private StoryBookParagraphDao storyBookParagraphDao;
    
    @Autowired
    private StoryBooksJsonService storyBooksJsonService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String handleRequest(HttpSession session, @PathVariable Long id) {
    	logger.info("handleRequest");
        
        Contributor contributor = (Contributor) session.getAttribute("contributor");
        logger.info("contributor.getRoles(): " + contributor.getRoles());
        if (!contributor.getRoles().contains(Role.ADMIN)) {
            // TODO: return HttpStatus.FORBIDDEN
            throw new IllegalAccessError("Missing role for access");
        }
        
        StoryBookParagraph storyBookParagraphToBeDeleted = storyBookParagraphDao.read(id);
        logger.info("storyBookParagraphToBeDeleted: " + storyBookParagraphToBeDeleted);
        
        // Delete the paragraph
        logger.info("Deleting StoryBookParagraph with ID " + storyBookParagraphToBeDeleted.getId());
        storyBookParagraphDao.delete(storyBookParagraphToBeDeleted);
        
        // Update the storybook's metadata
        StoryBook storyBook = storyBookParagraphToBeDeleted.getStoryBookChapter().getStoryBook();
        storyBook.setTimeLastUpdate(Calendar.getInstance());
        storyBook.setRevisionNumber(storyBook.getRevisionNumber() + 1);
        storyBookDao.update(storyBook);
        
        StoryBookContributionEvent storyBookContributionEvent = new StoryBookContributionEvent();
        storyBookContributionEvent.setContributor(contributor);
        storyBookContributionEvent.setTime(Calendar.getInstance());
        storyBookContributionEvent.setStoryBook(storyBook);
        storyBookContributionEvent.setRevisionNumber(storyBook.getRevisionNumber());
        storyBookContributionEvent.setComment("Deleted storybook paragraph (🤖 auto-generated comment)");
        storyBookContributionEventDao.create(storyBookContributionEvent);
        
        // Update the sorting order of the remaining paragraphs
        logger.info("storyBookParagraphToBeDeleted.getSortOrder(): " + storyBookParagraphToBeDeleted.getSortOrder());
        List<StoryBookParagraph> storyBookParagraphs = storyBookParagraphDao.readAll(storyBookParagraphToBeDeleted.getStoryBookChapter());
        logger.info("storyBookParagraphs.size(): " + storyBookParagraphs.size());
        for (StoryBookParagraph storyBookParagraph : storyBookParagraphs) {
            logger.info("storyBookParagraph.getId(): " + storyBookParagraph.getId() + ", storyBookParagraph.getSortOrder(): " + storyBookParagraph.getSortOrder());
            if (storyBookParagraph.getSortOrder() > storyBookParagraphToBeDeleted.getSortOrder()) {
                // Reduce sort order by 1
                storyBookParagraph.setSortOrder(storyBookParagraph.getSortOrder() - 1);
                storyBookParagraphDao.update(storyBookParagraph);
                logger.info("storyBookParagraph.getSortOrder() (after update): " + storyBookParagraph.getSortOrder());
            }
        }
        
        // Refresh the REST API cache
        storyBooksJsonService.refreshStoryBooksJSONArray();
        
        return "redirect:/content/storybook/edit/" + storyBook.getId() + "#ch-id-" + storyBookParagraphToBeDeleted.getStoryBookChapter().getId();
    }
}
