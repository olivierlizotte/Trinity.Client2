package graphDB.explore;

import graphDB.explore.tools.AlphanumComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

/** This class determines the default behavior of the explorer
 *
 */
abstract public class DefaultTemplate 
{
	/** Registers a shutdown hook for the Neo4j instance so that it
	    shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    running example before it's completed)
	 * @param graphDb
	 */
	public static void registerShutdownHook( final GraphDatabaseService graphDb, final String dbName )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running example before it's completed)
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
			{
	        	theGraphs.remove(dbName);
	        	CloseDB(graphDb);
			}
		} );
	}
		
	public static void removeAllTempElements(GraphDatabaseService graphDb )
	{
		try(Transaction tx = graphDb.beginTx())
		{
			Index<Node> index = graphDb.index().forNodes("tempNodes");
			IndexHits<Node> tempNodes = index.get("type", "tempNode");
			while (tempNodes.hasNext())
			{
				Node tempNode = tempNodes.next();
				Iterable<Relationship> tempRels = tempNode.getRelationships();
				for (Relationship rel : tempRels)
					rel.delete();
				tempNode.delete();
			}
			tx.success();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	public static void CloseDB(GraphDatabaseService graphDb)
	{
    	removeAllTempElements(graphDb);
    	if(graphDb != null)
    		graphDb.shutdown();
	}
	
	public static String GraphDBString_Main = "graphProjects.db";
	public static String MainSiteAdress = "localhost:8080/PeptidAce.WebClient/";
	
	private static HashMap<String, GraphDatabaseService> theGraphs = new HashMap<String, GraphDatabaseService>();
	
	
	synchronized public static GraphDatabaseService graphDb(String dbName)
	{
		if(!theGraphs.containsKey(dbName))
		{
			try
			{
				theGraphs.put(dbName, new GraphDatabaseFactory().newEmbeddedDatabase( dbName ));
				registerShutdownHook(theGraphs.get(dbName), dbName);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}			
		}
		return theGraphs.get(dbName);
	}
	
		
	/** This function determines whether an attribute should be displayed or not in the explorer
	 * @param theAttributeName attribute to test
	 * @return true if the attribute should be kept, false otherwise
	 */
	public static Boolean keepAttribute( String theAttributeName )
	{
		if("type".equals(theAttributeName) ||
				"StringID".equals(theAttributeName) ||
				"passwd".equals(theAttributeName) ||
				"created from id".equals(theAttributeName) ||
				"Peptidome_peptideLength".equals(theAttributeName)||
				"data".equals(theAttributeName)||
				"xfield".equals(theAttributeName)||
				"yfield".equals(theAttributeName)||
				"maxYaxis".equals(theAttributeName)||
				"queries".equals(theAttributeName))
			return false;
		return true;
	}
	
	/** This function determines whether an attribute is a Name attribute
	 * @param theAttributeName attribute to test
	 * @return true if the attribute is a name
	 */
	public static Boolean isNameAttribute( String theAttributeName )
	{
		if("name".equals(theAttributeName) ||
		   "Name".equals(theAttributeName))
			return true;
		return false;
	}

	/** This function determines whether an attribute should be displayed or not in the explorer
	 * @param theRelationName the relation to test
	 * @return true if the relation should be kept, false otherwise
	 */
	public static Boolean keepRelation( String theRelationName )
	{
		//if("Tool_output".equals(theRelationName) //|| "Comment".equals(theRelationName)				
			//|| theRelationName == "Hash"
		//		)
		//	return false;
		return true;
	}

	/** This function returns an ordered list of String based on a given key set
	 * @param keySet the key set of a hashmap, used to create the ordered attribute list
	 * @return the list of attributes, ordered
	 */
	public static List<String> sortAttributes(Set<String> keySet)
	{
		final AlphanumComparator alNum = new AlphanumComparator();
		List<String> results = new ArrayList<String>();
		results.addAll(keySet);
		Collections.sort(results, 
        		new Comparator<String>()
                {
                    public int compare( String n1, String n2 )
                    {
                    	return alNum.compare((String)n1, (String)n2);
                    }
                } );//*/
		return results;
	}
/*
	public static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running example before it's completed)
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
			{
	            graphDb.shutdown();
			}
		} );
	}//*/
	
	
	/** Get the tools according to the type of node currently diplayed.
	 * @param nodeID
	 * @param graphDb
	 * @return
	 */
	public static String[] getChartsTools( String nodeID , String dataName)
	{
		DefaultNode theNode = new DefaultNode(nodeID, dataName);
		String type = theNode.getType();
		if("Peptidome".equals(type))
		{
			String[] testE = {"applets/tools/PeptideLength", 
							  "applets/tools/DecoyAnalysis", 
							  "applets/tools/BindingScoreDistribution",
							  "applets/tools/MascotScoreDistribution",
							  "applets/tools/IntensityDistribution",
							  "applets/tools/PvalDistribution",
							  "applets/tools/SequenceRedundancy",
							  "applets/tools/HlaAlleleDistribution",
							  "applets/tools/SourceProteinsPerPeptides"};
			return testE;
		}
		if("ExpertMode_output".equals(type))
		{
			String[] testE = {"applets/tools/PeptideLength", 
							  "applets/tools/DecoyAnalysis", 
							  "applets/tools/BindingScoreDistribution",
							  "applets/tools/MascotScoreDistribution",
							  "applets/tools/IntensityDistribution",
							  "applets/tools/PvalDistribution",
							  "applets/tools/SequenceRedundancy", 
							  "applets/tools/HlaAlleleDistribution"};
			return testE;
		}
		if("EasyQuery_output".equals(type))
		{
			String[] testE = {"applets/tools/PeptideLength", 
							  "applets/tools/DecoyAnalysis", 
							  "applets/tools/BindingScoreDistribution",
							  "applets/tools/MascotScoreDistribution",
							  "applets/tools/IntensityDistribution",
							  "applets/tools/PvalDistribution",
							  "applets/tools/SequenceRedundancy",
							  "applets/tools/HlaAlleleDistribution"};
			return testE;
		}
		if("Sequence Search".equals(type))
		{
			String[] testE = {"applets/tools/PeptideLength",
					  		  "applets/tools/MascotScoreDistribution"};
			return testE;
		}
		if("User".equals(type))
		{			
			String[] testU = {"applets/tools/AddUser"};
			return testU;
		}			
		return new String[0];
	}
	
	public static String[] getNodeSpecificTools( String nodeID, String dataName )
	{
		DefaultNode theNode = new DefaultNode(nodeID, dataName);
		String type = theNode.getType();
		List<String> tools = new ArrayList<String>();
		if("Peptidome".equals(type))
		{
			tools.add("applets/tools/SequenceAnalysis");
			tools.add("applets/tools/EasyQuery");
		}
		if("Sequence Search".equals(type))
		{
			tools.add("applets/tools/SequenceAnalysis");
			tools.add("applets/tools/EasyQuery");
			tools.add("applets/tools/CsvExport");
		}
		if("Temporary Node".equals(type))
		{
			tools.add("applets/tools/DeleteNode");
		}
		
		if("EasyQuery_output".equals(type))
		{
			tools.add("applets/tools/SavePipeLine");
			tools.add("applets/tools/EasyQuery");
		}
		if("ExpertMode_output".equals(type))
		{
			tools.add("applets/tools/SavePipeLine");
			tools.add("applets/tools/EasyQuery");
		}
		if("Pipeline".equals(type))
		{
			
		}
		tools.add("applets/tools/DeleteNode");
		return tools.toArray(new String[tools.size()]);
	}
	
	/** Transform a Text referring to another node into a link to this node
	 * @param text String
	 * @param theNode neo4j Node
	 * @param theUser neo4j Node
	 * @param graphDb EmbeddedGraphDatabase
	 * @return
	 */
	public static String checkForHashTags(String text, Node theNode, Node theUser, GraphDatabaseService graphDb)
	{
	    StringBuffer sb = new StringBuffer(text.length());	
		
		Index<Node> index = graphDb.index().forNodes("hashtags");
		
		Pattern patt = Pattern.compile("(#[a-zA-Z_0-9]*)");//(#[^<]*?)[\\s|\\z]");
		Matcher m = patt.matcher(text);
	    while (m.find()) 
	    {
	    	String tag = m.group(1);

			Node tagNode = index.get("name", tag).getSingle();
			if(tagNode == null)
			{
				tagNode = graphDb.createNode();
				tagNode.setProperty("name", tag);		
				tagNode.setProperty("type", "HashTag");
				index.add(tagNode, "name", tag);
			}
			RelationshipType relType = DynamicRelationshipType.withName( "Hash" );	
			theNode.createRelationshipTo(tagNode, relType).setProperty("User", theUser.getProperty("NickName"));				
			m.appendReplacement(sb, Matcher.quoteReplacement("<a href=index.jsp?id=" + tagNode.getId() + ">" + tag + "</a> "));
	    }
	    m.appendTail(sb);	
	
		return sb.toString();
	}

	/** Cleans a string to make it DB ready and HTML friendly
	 * @param input String
	 * @return String sanitized
	 */
	public static String Sanitize(String input, boolean removeQuotes)
	{
		String text = input.replaceAll("\\r","<br/>");
		text = text.replaceAll("\\n","<br/>");
		if(removeQuotes)
		{
			text = text.replaceAll("\\\"", "&#34;");
			text = text.replaceAll("\\\\", "&#92;");
		}
		return text;
	}	
		
	/** Calculate number of elements for a grouping node such as 
	 * peptides for peptidome, proteins for proteome etc. 
	 */
	public static int numberOfElements(GraphDatabaseService graphDb, Node groupingNode, String nodeTypeToCount){
		int nb=0;
		for (Relationship rel : groupingNode.getRelationships(Direction.OUTGOING)){
			if (nodeTypeToCount.equals(NodeHelper.getType(rel.getEndNode()))){
				nb+=1;
			}
		}
		return nb;
	}
	
	/** Calculate FPR for a grouping node such as Peptidome, proteome. 
	 * The nodes OUTGING must have decoy properties. 
	 */
	public static double calculateFPR(GraphDatabaseService graphDb, Node groupingNode){
		Node tmpNode;
		double total = 0;
		double targetHits = 0;
		double decoyHits = 0;
		boolean hasDecoy=false;
		for (Relationship rel : groupingNode.getRelationships(Direction.OUTGOING)){
			tmpNode = rel.getEndNode();
			total+=1;
			if (tmpNode.hasProperty("Decoy")){
				hasDecoy=true;
				total += 1;
				if ("True".equals(tmpNode.getProperty("Decoy")))
					decoyHits += 1;
				else{
					targetHits += 1;
				}
			}
		}
		if (hasDecoy){
			groupingNode.setProperty("FPR (decoy hits/ target hits)", Double.valueOf(decoyHits/targetHits));
			groupingNode.setProperty("Decoy hits", Double.valueOf(decoyHits));
			groupingNode.setProperty("Total hits", total);
		}
		return total;
	}
	
	
	/** Add basic information to the database just after creating it: 
	 *  - Flase positive rate to Peptidome, Sequence Search and Quantification
	 *  - Number of nodes for each node grouped to many other.
	 *  - General Experiment's information: number of peptides, proteins etc.
	 */
	public static void addBasicInformation(GraphDatabaseService graphDb, Long experimentNodeId){
		Node experimentNode = graphDb.getNodeById(experimentNodeId);
		Node tmpNode;
		double total;
		
		for (Relationship rel : experimentNode.getRelationships(Direction.OUTGOING)){
			tmpNode = rel.getEndNode();
			if ("Peptidome".equals(NodeHelper.getType(tmpNode))){
				total = calculateFPR(graphDb, tmpNode);
				//if (!experimentNode.hasProperty("number of peptides")){
					experimentNode.setProperty("number of peptides", total);
				//}
			}
			if ("Sequence Search".equals(NodeHelper.getType(tmpNode))){
				total = calculateFPR(graphDb, tmpNode);
				//if (!experimentNode.hasProperty("number of peptide identifications")){
					experimentNode.setProperty("number of peptide identifications", total);
				//}
			}
			if ("Quantification".equals(NodeHelper.getType(tmpNode))){
				total = calculateFPR(graphDb, tmpNode);
				//if (!experimentNode.hasProperty("number of clusters")){
					experimentNode.setProperty("number of clusters", total);
				//}
			}
		}
	}
}
