class RegressionTree {
	private Node root;
	private RegressionTree[] childTree;

	RegressionTree(){}

	RegressionTree(Data trainingSet){
			
		learnTree(trainingSet,0,trainingSet.getNumberOfExamples()-1,trainingSet.getNumberOfExamples()*10/100);
	}
		
	boolean isLeaf(Data trainingSet,int begin, int end,int numberOfExamplesPerLeaf){
		int numberOfExamples = end - begin + 1;
    	return numberOfExamples <= numberOfExamplesPerLeaf;
	}

	SplitNode determineBestSplitNode(Data trainingSet, int begin, int end){

    	SplitNode bestSplitNode = null;

    	for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
        	DiscreteAttribute attribute=(DiscreteAttribute) trainingSet.getExplanatoryAttribute(i);
			SplitNode currentSplitNode=new DiscreteNode(trainingSet, begin, end, attribute);

        	if (bestSplitNode == null || currentSplitNode.getVariance() < bestSplitNode.getVariance()) {
				bestSplitNode = currentSplitNode;
        	}
        }

        trainingSet.sort(bestSplitNode.getAttribute(), begin, end);

        return bestSplitNode;
    }


	void learnTree(Data trainingSet,int begin, int end,int numberOfExamplesPerLeaf){
		if( isLeaf(trainingSet, begin, end, numberOfExamplesPerLeaf)){
			//determina la classe che compare pi� frequentemente nella partizione corrente
			root=new LeafNode(trainingSet,begin,end);
		}
		else //split node
		{
			root=determineBestSplitNode(trainingSet, begin, end);
			
			if(root.getNumberOfChildren()>1){
				childTree=new RegressionTree[root.getNumberOfChildren()];
				for(int i=0;i<root.getNumberOfChildren();i++){
					childTree[i]=new RegressionTree();
					childTree[i].learnTree(trainingSet, ((SplitNode)root).getSplitInfo(i).beginIndex, ((SplitNode)root).getSplitInfo(i).endIndex, numberOfExamplesPerLeaf);
				}
			}
			else
				root=new LeafNode(trainingSet,begin,end);
				
		}
	}
			

		
	void printTree(){
		System.out.println("********* TREE **********\n");
		System.out.println(toString());
		System.out.println("*************************\n");
	}
		
	public String toString(){
		String tree=root.toString()+"\n";
			
		if( root instanceof LeafNode){
			
		}
		else //split node
		{
			for(int i=0;i<childTree.length;i++)
				tree +=childTree[i];
		}
		return tree;
	}

	void printRules(){
    	System.out.println("********* RULES **********");
    	printRules("");
		System.out.println("*************************\n");
    }

    void printRules(String current){
		if (root instanceof LeafNode) {
			Double predictedClassValue=((LeafNode) root).getPredictedClassValue();
			if (current.isEmpty()){
				System.out.println("Class=" + predictedClassValue);
			}
			else{
				System.out.println(current + " ==> Class=" + predictedClassValue);
			}
		} 
		else{

			SplitNode splitNode=(SplitNode) root;

			for (int i = 0; i < splitNode.getNumberOfChildren(); i++) {

				String condition = splitNode.getAttribute().getName() + splitNode.getSplitInfo(i).getComparator() + splitNode.getSplitInfo(i).getSplitValue();
				String newCurrent;

				if (current.isEmpty()){
					newCurrent = condition;
				} 
				else{
					newCurrent =current + " AND " + condition;
				}
				childTree[i].printRules(newCurrent);
			}
    	}
    }	
}
		
