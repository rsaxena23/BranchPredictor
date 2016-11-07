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
        tagArray = new TagEntry[btbLength][assoc];
    }

    public String[] getTranslatedValues(String addr)
    {
        String values[]=new String[2];


        return values;
    }

    public boolean inBTB(int index, String tag)
    {
        return false;
    }
}
