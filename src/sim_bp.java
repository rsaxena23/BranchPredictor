import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by raunaq on 11/7/16.
 */


public class sim_bp {

    public void startSimulate(BTB btbCache, Predictor pred, String traceFile)
    {
        String line="",values[],address,btbTranslate[];
        boolean checkPredictor = true,outcome, predOutcome;
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(traceFile));
            while((line=br.readLine())!=null)
            {
                values = line.split(" ");
                address = values[0];
                outcome = values[1].equals("t");
                if(btbCache!=null)
                {
                    btbTranslate = btbCache.getTranslatedValues(address);
                    checkPredictor = btbCache.inBTB( Integer.parseInt(btbTranslate[0]) , btbTranslate[1]);
                    if (outcome!=checkPredictor)
                        btbCache.misses+=1;
                    else
                        btbCache.hits+=1;
                }
                if(checkPredictor)
                {
                    pred.totalPredictions+=1;
                    predOutcome = pred.predict(address,outcome);
                    if(predOutcome!=outcome)
                        pred.misses+=1;
                    else
                        pred.hits+=1;
                }
            }

        }catch(Exception e)
        {
            System.out.println("Some error:"+e.getMessage());
        }
    }

    public static void main(String[] args)
    {
        int argLength,hValue=0,indexPred=0,indexBTB=0,assocBTB=0;
        String predictorType="",traceFile="";
        BTB btbCache = null;
        Predictor pred = null;

        argLength=args.length;

        if (argLength==0)
        {
            System.out.println("Not enough arguments");
            System.exit(0);
        }

        predictorType = args[0];

        if(predictorType.equals("bimodal") && argLength>4)
        {
            indexPred = Integer.parseInt(args[1]);
            indexBTB = Integer.parseInt(args[2]);
            assocBTB = Integer.parseInt(args[3]);
            traceFile = args[4];

            pred = new Predictor(indexPred,0,Constants.BIMODIAL);

            if ( indexBTB!=0 && assocBTB!=0 )
                btbCache = new BTB(indexBTB,assocBTB);

        }
        else if(predictorType.equals("gshare") && argLength>5)
        {
            indexPred = Integer.parseInt(args[1]);
            hValue = Integer.parseInt(args[2]);
            indexBTB = Integer.parseInt(args[3]);
            assocBTB = Integer.parseInt(args[4]);
            traceFile = args[5];

            pred = new Predictor(indexPred,hValue,Constants.GSHARE);

            if ( indexBTB!=0 && assocBTB!=0 )
                btbCache = new BTB(indexBTB,assocBTB);

        }
        else if(predictorType.equals("hybrid") && argLength>6)
        {

        }
        else
        {
            System.out.println("Not enough arguments");
            System.exit(0);
        }

        new sim_bp().startSimulate(btbCache,pred,traceFile);

    }
}
