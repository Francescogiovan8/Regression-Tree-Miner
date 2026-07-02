package tree;

import data.Data;

class LeafNode extends Node {
    private Double predictedClassValue=0.0;

    LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex){
        super(trainingSet, beginExampleIndex, endExampleIndex);

        for(int i=beginExampleIndex;i<=endExampleIndex;i++){
            predictedClassValue+=trainingSet.getClassValue(i);
        }
        predictedClassValue=predictedClassValue/(endExampleIndex-beginExampleIndex+1);
    }

    Double getPredictedClassValue(){
        return predictedClassValue;
    }

    int getNumberOfChildren(){
        return 0;
    }

    @Override
    public String toString(){
        return "LEAF : class="+getPredictedClassValue()+" "+super.toString();
    }
}
