package it.polito.tdp.PremierLeague.model;

import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private PremierLeagueDAO dao;
	private Graph<Match, DefaultWeightedEdge> grafo;
	private Map<Integer, Match> idMap;
	private List<Match> soluzioneMigliore;
	
	public Model() {
		dao = new PremierLeagueDAO();
		idMap = new HashMap<>();
		dao.listAllMatches(idMap);
	}
	
	
	public void creaGrafo(int mese, int min) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		// aggiungo vertici
		Graphs.addAllVertices(this.grafo, this.dao.getPartiteDelMese(mese, idMap));
		
		// aggiungo archi
		for(Arco a: dao.getArchi(mese, min, idMap)) {
			Graphs.addEdge(this.grafo, a.getM1(), a.getM2(), a.getPeso());
		}
		
	}
	
	public int getNVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int getNArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public List<Arco> getMatchMigliori(){
		List<Arco> migliori = new LinkedList<>();
		int max = 0;
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(grafo.getEdgeWeight(e) > max) {
				max = (int) grafo.getEdgeWeight(e);
			}
		}
		
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(grafo.getEdgeWeight(e) == max) {
				migliori.add(new Arco(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), (int) this.grafo.getEdgeWeight(e)));
			}
		}
		return migliori;
		
	}
	
	public Set<Match> getVertici(){
		return grafo.vertexSet();
	}
	
	public List<Match> getPercorso(Match partenza, Match arrivo){
		soluzioneMigliore = new ArrayList<>();
		List<Match> parziale = new ArrayList<>();
		
		cerca(partenza, arrivo, parziale);
		
		return soluzioneMigliore;
	}
	
	public void cerca(Match partenza, Match arrivo, List<Match> parziale) {
		
		// non si può passare da match che hanno le squadre definite in arrivo e partenza 
		
		if(parziale.size() > 0 && parziale.get(parziale.size()-1).equals(arrivo)) {
			if(parziale.size() > this.soluzioneMigliore.size()) {
				this.soluzioneMigliore = new LinkedList<>(parziale);
			}
		}
		
		for(Match vicino: Graphs.successorListOf(grafo, parziale.get(parziale.size()-1))) {
			if(!parziale.contains(vicino)) {
				// se non l'ho mai visto prima provo ad aggiungerlo
				if((!vicino.getTeamHomeID().equals(partenza.getTeamHomeID()) && !vicino.getTeamAwayID().equals(partenza.getTeamHomeID())
						&& !vicino.getTeamHomeID().equals(partenza.getTeamAwayID()) && !vicino.getTeamAwayID().equals(partenza.getTeamAwayID())))
				parziale.add(vicino);
				cerca(partenza, arrivo, parziale);
				parziale.remove(parziale.size()-1);
			}
		}
	}
}
