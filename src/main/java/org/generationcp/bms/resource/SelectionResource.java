package org.generationcp.bms.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.GermplasmScoreCard;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/selection")
public class SelectionResource {	
	
	private final SimpleDao simpleDao;
	
	@Autowired
	public SelectionResource(SimpleDao simpleDao){
		this.simpleDao = simpleDao;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(HttpServletRequest request) throws MiddlewareQueryException {
		return "Please provide parameters for selection in format host:port/selection/top/{trialId}/{selectionIntensity}";
	}
	
	@RequestMapping(value="/top/{trialId}/{selectionIntensity}/{traits}", method = RequestMethod.GET)
	public List<GermplasmScoreCard> selectTopPerformers(@PathVariable Integer trialId, @PathVariable int selectionIntensity, @PathVariable String traits) {
		
		List<TraitInfo> traitList = new ArrayList<TraitInfo>();
		String[] traitsIds = traits.split(",");
		for (int i = 0; i < traitsIds.length; i++) {
			traitList.add(new TraitInfo(Integer.valueOf(traitsIds[i]).intValue()));
		}
		
		List<GermplasmScoreCard> scoreCards = simpleDao.getTraitObservationsForTrial(trialId, traitList);
		Collections.sort(scoreCards);
		
		//select the top designated percentage of the sorted collection
		int selectionThreshold = (int) (scoreCards.size()*selectionIntensity/100);
		return scoreCards.subList(0, selectionThreshold);
		
	}

}