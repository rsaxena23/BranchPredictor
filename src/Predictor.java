/**
 * Created by raunaq on 11/7/16.
 */
public class Predictor {
    int hValue;
    int iValue;
    int kValue;
    long globalHvalue = 0;

    int totalPredictions=0;
    int hits=0;
    int misses=0;
    short predictArray[];

    int predictorType=0;

    Predictor gPred,bPred;

    public Predictor(int iValue,int hValue,int predictorType)
    {
        this.iValue = iValue;
        this.hValue = hValue;
        this.predictorType = predictorType;
        this.predictArray = new short[(int)Math.pow(2,iValue)];

        //initialize the Predict Table
        for(int i=0;i<predictArray.length;i++)
            predictArray[i]=2;
    }
    public Predictor(int kValue, int predictorType, Predictor gPred, Predictor bPred)
    {
        this.predictorType = predictorType;
        this.kValue = kValue;
        this.gPred = gPred;
        this.bPred = bPred;
        this.predictArray = new short[(int)Math.pow(2,kValue)];

        for(int i=0;i<predictArray.length;i++)
            predictArray[i]=1;
    }

    public long getTranslatedValue(String address)
    {
        long result;
        long addr = Long.parseLong(address,16);
        addr = addr>>2;
        //System.out.println("addr: "+address+" >>2: "+Long.toHexString(addr));
        //indexValue
        long iPart =  (addr&((long)(Math.pow(2,iValue-hValue) - 1)  ));
        long hPart = (hValue>0)?((addr>>(iValue-hValue))&((long)(Math.pow(2,hValue) - 1)  )):0;
        //System.out.println("H: "+Long.toBinaryString(hPart)+" GHR: "+Long.toBinaryString(globalHvalue));
        if (hValue>0)
            hPart = hPart^globalHvalue;
        result = (hPart<<(iValue-hValue))+iPart;
        //System.out.println("Ex-orH: "+Long.toBinaryString(hPart)+" + i: "+Long.toBinaryString(iPart)+" = "+Long.toBinaryString(result));
        return result;
    }

    public void updateHValue(int value)
    {
        //System.out.println("prev:"+Long.toBinaryString(globalHvalue)+" value:"+value);
        globalHvalue = (globalHvalue>>1);
        if(value==1)
            globalHvalue= globalHvalue|(1<<(hValue-1));
        else
            globalHvalue= globalHvalue&(((long)(Math.pow(2,hValue-1) - 1)  ));
        //System.out.println("new:"+Long.toBinaryString(globalHvalue));

    }

    public boolean predict(String address,boolean outcome, int predictorType)
    {
        int index = (int)getTranslatedValue(address);
        boolean result;

        if (index> predictArray.length)
            System.out.println("Translation Problem");

        if(predictArray[index]>=2)
            result = true;
        else
            result = false;
        if(predictorType!=Constants.HYBRID) {
            if (outcome)
                predictArray[index] += (predictArray[index] < 3) ? 1 : 0;
            else
                predictArray[index] -= (predictArray[index] > 0) ? 1 : 0;
        }

        if(hValue>0)
            updateHValue((outcome)?1:0);

        return result;
    }

    public boolean predictHybrid(String address,boolean outcome)
    {
        long gIndex = gPred.getTranslatedValue(address);
        boolean gPrediction = gPred.predict(address,outcome,predictorType);
        boolean bPrediction = bPred.predict(address,outcome,predictorType);
        boolean result;
        long addr = Long.parseLong(address,16);
        int kIndex =(int)((addr>>2)&((long)(Math.pow(2,kValue)-1)));
        //System.out.println("\n"+address+" >>2 "+Long.toHexString(addr>>2)+" "+Long.toHexString(kIndex));
        //System.out.println(" index:"+kIndex+" choice:"+predictArray[kIndex]+" g:"+gPrediction+" b:"+bPrediction+" res:"+outcome);



        if(predictArray[kIndex]>=2) {
            gPred.updatePredictTable((int) gIndex, outcome);
            result = gPrediction;
        }
        else {
            bPred.updatePredictTable((int) bPred.getTranslatedValue(address), outcome);
            result = bPrediction;
        }

        if(gPrediction!=bPrediction)
        {
            if(gPrediction==outcome)
                predictArray[kIndex]+= (predictArray[kIndex]<3)?1:0;
            else
                predictArray[kIndex]-= (predictArray[kIndex]>0)?1:0;
        }
        //System.out.println("after:"+predictArray[kIndex]+" ghr:"+gPred.globalHvalue);
        return result;
    }

    public void updatePredictTable(int index,boolean outcome)
    {
        //System.out.println("array before:"+predictArray[index]);

        if (outcome)
            predictArray[index]+= (predictArray[index]<3)?1:0;
        else
            predictArray[index]-= (predictArray[index]>0)?1:0;

        //System.out.println("array after:"+predictArray[index]);
    }

}
