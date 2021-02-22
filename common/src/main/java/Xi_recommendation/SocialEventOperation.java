package Xi_recommendation;

import java.util.ArrayList;

public class SocialEventOperation {


    void SetEventUserIdsFre(ArrayList<SubEvent> Eventclusters)
    {
        for(SubEvent ecit:Eventclusters) {
//            ecit.setEventUserIDs();
        }
    }

    public float l2_dist_int(int[] _p1, int[] _p2, int _dim)
    {
        float ret = 0;
        for (int i = 0; i < _dim; i++)
        {
            float dif = (float)(_p1[i] - _p2[i]);
            ret += dif * dif;
        }
        return ret;
    }

}

