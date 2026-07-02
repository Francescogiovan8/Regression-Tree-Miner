package tree;

import data.Attribute;
import data.Data;
import data.DiscreteAttribute;

class DiscreteNode extends SplitNode {
    public DiscreteNode(Data trainingSet,int beginExampleIndex, int endExampleIndex,DiscreteAttribute attribute){
        super(trainingSet,beginExampleIndex,endExampleIndex,attribute);
    }

    void setSplitInfo(Data trainingSet,int beginExampleIndex, int endExampleIndex, Attribute attribute){
        int attributeIndex = attribute.getIndex();

        int numberOfDistinctValues = 1;

        for (int i=beginExampleIndex+1; i <= endExampleIndex; i++) {
            Object previousValue = trainingSet.getExplanatoryValue(i - 1, attributeIndex);

            Object currentValue =trainingSet.getExplanatoryValue(i, attributeIndex);

            if (!currentValue.equals(previousValue)) {
                numberOfDistinctValues++;
            }
        }

        mapSplit = new SplitInfo[numberOfDistinctValues];

        int numberChild = 0;
        int beginSplitIndex = beginExampleIndex;

        Object currentSplitValue = trainingSet.getExplanatoryValue(beginExampleIndex, attributeIndex);

        for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {

            Object currentValue = trainingSet.getExplanatoryValue(i, attributeIndex);

            if (!currentValue.equals(currentSplitValue)) {
                mapSplit[numberChild] = new SplitInfo(currentSplitValue, beginSplitIndex, i - 1, numberChild);
                numberChild++;
                beginSplitIndex = i;
                currentSplitValue = currentValue;
            }
        }

        mapSplit[numberChild] = new SplitInfo(currentSplitValue, beginSplitIndex, endExampleIndex, numberChild);
    }

    int testCondition (Object value){
        for (int i = 0; i < mapSplit.length; i++) {
            if (mapSplit[i].getSplitValue().equals(value)) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public String toString(){
        return "DISCRETE "+super.toString();
    }
}
