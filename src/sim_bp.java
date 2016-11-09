import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by raunaq on 11/7/16.
 */


public class sim_bp {

    int totalPredictions = 0;

    public void startSimulate(BTB btbCache, Predictor pred, String traceFile)
    {
        String line,values[],address="",btbTranslate[];
        boolean checkPredictor = true,outcome, predOutcome;
        //int counter=0;
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(traceFile));
            while((line=br.readLine())!=null)
            {
                totalPredictions+=1;
                values = line.split(" ");
                address = values[0];
                outcome = values[1].equals("t");
                if(btbCache!=null)
                {
                    btbTranslate = btbCache.getTranslatedValues(address);
                    checkPredictor = btbCache.inBTB( Integer.parseInt(btbTranslate[0]) , btbTranslate[1]);
                    if (outcome!=checkPredictor && outcome==true)
                        btbCache.misses+=1;
                    else
                        btbCache.hits+=1;
                }
                if(checkPredictor)
                {
                    pred.totalPredictions+=1;
                    if(pred.predictorType!=Constants.HYBRID)
                        predOutcome = pred.predict(address,outcome,pred.predictorType);
                    else
                        predOutcome = pred.predictHybrid(address,outcome);

                    if(predOutcome!=outcome)
                        pred.misses+=1;
                    else
                        pred.hits+=1;
                }
                /*counter++;
                if (counter>100)
                    break;*/
            }

        }catch(Exception e)
        {
            System.out.println("Some error:"+e.getMessage());
            e.printStackTrace();
            System.out.println("Address:"+address);
        }
    }

    public void printResults(Predictor pred, BTB btbCache)
    {
        int a,b,c,d,i,j;
        double e;
        TagEntry tempEntry;
        a=totalPredictions;
        b=pred.totalPredictions;
        c=pred.misses;
        d=(btbCache!=null)?btbCache.misses:0;
        e=(double)(c+d)/(double)a;

        if(btbCache!=null)
        {
            System.out.println("\nFinal BTB Tag Array Contents {valid, pc}:");
            for(i=0;i<btbCache.tagArray.length;i++)
            {
                System.out.print("Set "+i+": ");
                for(j=0;j<btbCache.assoc;j++)
                {
                    tempEntry = btbCache.tagArray[i][j];
                    System.out.print(" {"+((tempEntry.valid)?1:0)+", 0x "+tempEntry.value+"} ");
                }
                System.out.println();
            }

        }

        if(pred.predictorType==Constants.HYBRID)
        {
            System.out.println("\nFinal Bimodal Table Contents:");
            for(i=0;i<pred.bPred.predictArray.length;i++)
                System.out.println("table["+i+"]: "+pred.bPred.predictArray[i]);

            System.out.println("\nFinal Gshare Table Contents:");
            for(i=0;i<pred.gPred.predictArray.length;i++)
                System.out.println("table["+i+"]: "+pred.gPred.predictArray[i]);
            System.out.println("Final GHR Contents: 0x "+Long.toHexString(pred.globalHvalue));

            System.out.println("\nFinal Chooser Table Contents:");
            for(i=0;i<pred.predictArray.length;i++)
                System.out.println("Choice table["+i+"]: "+pred.predictArray[i]);
        }
        else
        {
            String predName;
            if(pred.predictorType==Constants.BIMODIAL)
                predName="Bimodal";
            else
                predName="Gshare";

            System.out.println("\nFinal "+predName+" Table Contents:");
            for(i=0;i<pred.predictArray.length;i++)
                System.out.println("table["+i+"]: "+pred.predictArray[i]);
        }

        if(pred.predictorType==Constants.GSHARE)
            System.out.println("Final GHR Contents: 0x "+Long.toHexString(pred.globalHvalue));

        System.out.println("\nFinal Branch Predictor Statistics:");
        System.out.println("a. Number of branches: "+a);
        System.out.println("b. Number of predictions from the branch predictor: "+b);
        System.out.println("c. Number of mispredictions from the branch predictor: "+c);
        System.out.println("d. Number of mispredictions from the BTB predictor: "+d);
        System.out.println("e. Branch misprediction rate: "+(Math.round(e*100)/100.0));
    }

    public static void main(String[] args)
    {
        int argLength,hValue=0,kValue=0,indexPred=0,indexBTB=0,assocBTB=0,indexPredBimodal=0;
        String predictorType="",traceFile="";
        BTB btbCache = null;
        Predictor pred = null,gPred=null,bPred=null;

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

            System.out.println("Command Line\n./sim_bp "+args[0]+" "+args[1]+" "+args[2]+" "+args[3]+" "+args[4]);

        }
        else if(predictorType.equals("gshare") && argLength>5)
        {
            indexPred = Integer.parseInt(args[1]);
            hValue = Integer.parseInt(args[2]);
            indexBTB = Integer.parseInt(args[3]);
            assocBTB = Integer.parseInt(args[4]);
            traceFile = args[5];

            pred = new Predictor(indexPred,hValue,Constants.GSHARE);

            //System.out.println("i: "+indexPred+" h:"+hValue);

            if ( indexBTB!=0 && assocBTB!=0 )
                btbCache = new BTB(indexBTB,assocBTB);

            System.out.println("Command Line\n./sim_bp "+args[0]+" "+args[1]+" "+args[2]+" "+args[3]+" "+args[4]+" "+args[5]);

        }
        else if(predictorType.equals("hybrid") && argLength>6)
        {
            kValue = Integer.parseInt(args[1]);
            indexPred = Integer.parseInt(args[2]);
            hValue = Integer.parseInt(args[3]);
            indexPredBimodal = Integer.parseInt(args[4]);
            indexBTB = Integer.parseInt(args[5]);
            assocBTB = Integer.parseInt(args[6]);
            traceFile = args[7];

            gPred = new Predictor(indexPred,hValue,Constants.GSHARE);
            bPred = new Predictor(indexPredBimodal,0,Constants.BIMODIAL);
            pred = new Predictor(kValue, Constants.HYBRID, gPred, bPred );

            //System.out.println("i: "+indexPred+" h:"+hValue);

            if ( indexBTB!=0 && assocBTB!=0 )
                btbCache = new BTB(indexBTB,assocBTB);

            System.out.println("Command Line\n./sim_bp "+args[0]+" "+args[1]+" "+args[2]+" "+args[3]+" "+args[4]+" "+args[5]+" "+args[6]+" "+args[7]);

        }
        else
        {
            System.out.println("Not enough arguments");
            System.exit(0);
        }

        sim_bp obj = new sim_bp();
        obj.startSimulate(btbCache,pred,traceFile);
        obj.printResults(pred,btbCache);
    }
}
