package course.examples.draw;

/**
 * Created by South on 11/30/2016.
 */

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new RectsDrawingView(this));
    }

}