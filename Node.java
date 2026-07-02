abstract class Node{
    private static int idNodeCount=0; 
    private int idNode; 
    private int beginExampleIndex; 
    private int endExampleIndex; 
    private double variance; 

    protected Node(Data trainingSet, int beginExampleIndex, int endExampleIndex){
        this.beginExampleIndex=beginExampleIndex;
        this.endExampleIndex=endExampleIndex;
        idNode=idNodeCount++;
        variance = calculateSSE(trainingSet);
    }

    private double calculateSSE(Data trainingSet){
        int size=getEndExampleIndex()-getBeginExampleIndex()+1;
        double value;
        double sum=0;
        double sumSquare=0;

        for(int i=getBeginExampleIndex();i<=getEndExampleIndex();i++){
            value=trainingSet.getClassValue(i);
            sum+=value;
            sumSquare+=value*value;
        }

        return sumSquare-(sum*sum/size);
    }

    int getIdNode(){
        return idNode;
    }

    int getBeginExampleIndex(){
        return beginExampleIndex;
    }
    
    int getEndExampleIndex(){
        return endExampleIndex;
    }

    double getVariance(){
        return variance;
    }

    abstract int getNumberOfChildren();

    @Override
    public String toString(){
        return "Nodo: [Examples:"+beginExampleIndex+"-"+endExampleIndex+"] variance:"+variance;
    }
}