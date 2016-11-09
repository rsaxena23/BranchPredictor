/**
 * Created by raunaq on 11/7/16.
 */
public class BTB {

    int btbLength;
    int assoc;
    /*Essentially the BTB*/
    TagEntry tagArray[][];
    /*Used for LRU to denote time of Access */
    static int sysTimer=0;
    int hits=0;
    int misses=0;

    public BTB(int btbLength,int assoc)
    {
        this.btbLength = btbLength;
        this.assoc = assoc;
        int tagArrayLength = (int)Math.pow(2,this.btbLength);
        tagArray = new TagEntry[tagArrayLength][assoc];
        for(int i=0;i<tagArrayLength;i++) {
            for (int j = 0; j < this.assoc; j++) {
                tagArray[i][j] = new TagEntry();
            }
        }
    }

    public String[] getTranslatedValues(String address)
    {
        String values[]=new String[2];
        long addr = Long.parseLong(address,16);
        addr = addr>>2;
        values[0] = (addr&((long)(Math.pow(2,btbLength)-1)))+"";
        values[1] = Long.toHexString( (addr>>btbLength)  );
        //System.out.println(Long.toHexString(addr)+" index:"+values[0]);
        return values;
    }

    public boolean inBTB(int index, String tag,String pcTag)
    {
        int i;
        TagEntry tempEntry=null;
        for(i=0;i<assoc;i++)
        {
            tempEntry = tagArray[index][i];
            if(!tempEntry.valid || tempEntry.value==null)
                break;
            else if(tempEntry.value.equals(tag))
            {
                sysTimer+=1;
                tempEntry.accessTime =sysTimer;
                return true;
            }
        }

        if(i<assoc)
        {
            tempEntry.valid = true;
            tempEntry.value=tag;
            tempEntry.pcValue = pcTag;
            sysTimer+=1;
            tempEntry.accessTime = sysTimer;
        }
        else
            replace(index,tag,pcTag);
        return false;
    }

    public void replace(int index,String tag, String pcTag)
    {
        int minValue = tagArray[index][0].accessTime;
        int minPos = 0;
        TagEntry tempEntry = null;
        for(int i=1;i<assoc;i++)
        {
            tempEntry = tagArray[index][i];
            if(tempEntry.accessTime<minValue)
            {
                minValue = tempEntry.accessTime;
                minPos = i;
            }
        }

        tagArray[index][minPos].value = tag;
        tagArray[index][minPos].pcValue = pcTag;
        tagArray[index][minPos].valid = true;
        sysTimer+=1;
        tagArray[index][minPos].accessTime = sysTimer;

    }
}
