public class MissingValues implements Comparable
{
    private String string;
    private int missingValues;

    public MissingValues(String string, int missingValues)
    {
        this.missingValues = missingValues;
        this.string = string;
    }

    public String printMissingValuesString()
    {
        return this.string;
    }

    public int getMissingValues()
    {
        return this.missingValues;
    }

    //Eftersom klassen implementerar Javas inbyggda Comparable-interface behöver den en override för metoden
    //compareTo() som specificerar vilken typ av objekt som ska sorteras och enligt vilket kriterium (i det
    //här fallet vill vi ha objekten sorterade i fallande ordning enligt antal saknade värden) :
    @Override
    public int compareTo(Object missingValues)
    {
        int compareMissingValues = ((MissingValues) missingValues).getMissingValues();
        return compareMissingValues-this.missingValues;
    }
}
