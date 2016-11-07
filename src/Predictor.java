/**
 * Created by raunaq on 11/7/16.
 */
public class Predictor {
    int hValue;
    int iValue;
    long globalHvalue = 0;

    int totalPredictions=0;
    int hits=0;
    int misses=0;
    short predictArray[];

    int predictorType=0;

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

    public long getTranslatedValue(String address)
    {
        long result;
        long addr = Long.parseLong(address);
        addr = addr>>2;
        //indexValue
        long iPart =  (addr&((long)(Math.pow(2,iValue-hValue) - 1)  ));
        long hPart = (hValue>0)?((addr>>(iValue-hValue))&((long)(Math.pow(2,hValue) - 1)  )):0;
        if (hValue>0)
            hPart = hPart^globalHvalue;
        result = (hPart<<(iValue-hValue))+iPart;
        return result;
    }

    public void updateHValue(int value)
    {
        globalHvalue = (globalHvalue<<1)&((long)(Math.pow(2,hValue) - 1)  );
        if(value==1)
            globalHvalue= globalHvalue|(1<<(hValue-1));
        else
            globalHvalue= globalHvalue&(1<<((long)(Math.pow(2,hValue-1) - 1)  ));

    }

    public boolean predict(String address,boolean outcome)
    {
        int index = (int)getTranslatedValue(address);
        boolean result;

        if (index> predictArray.length)
            System.out.println("Translation Problem");

        if(predictArray[index]>=2)
            result = true;
        else
            result = false;

        if (outcome)
            predictArray[index]+= (predictArray[index]<3)?1:0;
        else
            predictArray[index]-= (predictArray[index]>0)?1:0;

        if(hValue>0)
            updateHValue((outcome)?1:0);

        return result;

    }

}
