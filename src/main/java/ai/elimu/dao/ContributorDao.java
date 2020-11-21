package ai.elimu.dao;

import java.util.Calendar;
import java.util.List;
import ai.elimu.model.contributor.Contributor;

import org.springframework.dao.DataAccessException;

public interface ContributorDao extends GenericDao<Contributor> {
	
    Contributor read(String email) throws DataAccessException;
    
    Contributor readByProviderIdGitHub(String id) throws DataAccessException;
    
    List<Contributor> readAllOrderedDesc() throws DataAccessException;
    
    List<Contributor> readAll(Calendar calendarFrom, Calendar calendarTo) throws DataAccessException;
    
    List<Contributor> readAllWithStoryBookContributions() throws DataAccessException;
    
    List<Contributor> readAllWithWordContributions() throws DataAccessException;
    
    List<Contributor> readAllWithNumberContributions() throws DataAccessException;
}
