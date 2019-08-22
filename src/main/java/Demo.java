import java.util.ArrayList;
import java.util.List;

public class Demo {

    private int totalsize;
    private int threadcount;

    public static void main(String[] args) {

        List<Thread> list = new ArrayList<>();






    }

    private void download(){
        int more = totalsize%threadcount;
        if(more>0){
            threadcount++;
        }

    }
}
