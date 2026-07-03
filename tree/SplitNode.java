package tree;

import data.Attribute;
import data.Data;

import java.util.ArrayList;
import java.util.List;

abstract class SplitNode extends Node implements Comparable<SplitNode>  {
	// Classe che colelzione informazioni descrittive dello split
	class SplitInfo{
		Object splitValue;
		int beginIndex;
		int endIndex;
		int numberChild;
		String comparator="=";

		SplitInfo(Object splitValue,int beginIndex,int endIndex,int numberChild){
			this.splitValue=splitValue;
			this.beginIndex=beginIndex;
			this.endIndex=endIndex;
			this.numberChild=numberChild;
		}

		SplitInfo(Object splitValue,int beginIndex,int endIndex,int numberChild, String comparator){
			this.splitValue=splitValue;
			this.beginIndex=beginIndex;
			this.endIndex=endIndex;
			this.numberChild=numberChild;
			this.comparator=comparator;
		}

		int getBeginindex(){
			return beginIndex;			
		}

		int getEndIndex(){
			return endIndex;
		}

		 Object getSplitValue(){
			return splitValue;
		}

		public String toString(){
			return "child " + numberChild +" split value"+comparator+splitValue + "[Examples:"+beginIndex+"-"+endIndex+"]";
		}
		
		String getComparator(){
			return comparator;
		}

	}

	Attribute attribute;	

	List<SplitInfo> mapSplit = new ArrayList<>();

	double splitVariance;
	
	abstract void setSplitInfo(Data trainingSet,int beginExampelIndex, int endExampleIndex, Attribute attribute);

	abstract int testCondition (Object value);
	
	SplitNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute){
			super(trainingSet, beginExampleIndex,endExampleIndex);
			this.attribute=attribute;
			trainingSet.sort(attribute, beginExampleIndex, endExampleIndex); // order by attribute
			setSplitInfo(trainingSet, beginExampleIndex, endExampleIndex, attribute);
						
			//compute variance
			splitVariance=0;
			for(int i=0;i<mapSplit.size();i++){
					double localVariance=new LeafNode(trainingSet, mapSplit.get(i).getBeginindex(),mapSplit.get(i).getEndIndex()).getVariance();
					splitVariance+=(localVariance);
			}
	}
	
	Attribute getAttribute(){
		return attribute;
	}
	
	double getVariance(){
		return splitVariance;
	}
	
	int getNumberOfChildren(){
		return mapSplit.size();
	}
	
	SplitInfo getSplitInfo(int child){
		return mapSplit.get(child);
	}

	
	String formulateQuery(){
		String query = "";
		for(int i=0;i<mapSplit.size();i++)
			query+= (i + ":" + attribute + mapSplit.get(i).getComparator() +mapSplit.get(i).getSplitValue())+"\n";
		return query;
	}
	
	public String toString(){
		String v= "SPLIT : attribute=" +attribute +" "+ super.toString()+  " Split Variance: " + getVariance()+ "\n" ;
		
		for(int i=0;i<mapSplit.size();i++){
			v+= "\t"+mapSplit.get(i)+"\n";
		}
		
		return v;
	}

	@Override
	public int compareTo(SplitNode o) {
		if (splitVariance < o.splitVariance) return -1;
		else if (splitVariance > o.splitVariance) return 1;
		else return 0;
	}
}
