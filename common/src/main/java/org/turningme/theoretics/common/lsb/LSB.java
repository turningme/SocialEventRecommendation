package org.turningme.theoretics.common.lsb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.turningme.theoretics.common.RecContext;
import org.turningme.theoretics.common.rand.Rand;

/**
 * Created by jpliu on 2020/2/24.
 */
public class LSB  implements Serializable {
    //--=== on disk ===--
    public int			t;								/* largest coordinate of a dimension */
    public int			d;								/* dimensionality */
    public int			n;								/* cardinality */
    public int			B;								/* page size in words */
    public int			ratio;							/* approximation ratio */

    //--=== debug ==--
    public int			quiet;
    public boolean		emergency;

    //--=== others ===--

    public String		dsPath ;					/* folder containing the dataset file */
    public char[]		dsName= new char[100];					/* dataset file name */
    public char[]		forestPath= new char[100];				/* folder containing the forest */

    public int			f;
    public int			pz;								/* number of pages to store the Z-value of a point */
    public int			L;								/* number of lsb-trees */
    public int			m;								/* dimensionality of the hash space */
    public int			qNumTrees;						/* number of lsb-trees used to answer a query */
    public int			u;								/* log_2 (U/w) */

    public float[]		 a_array;
    public float[] b_array;						/* each lsb-tree requires m hash functions, and each has function
												   requires a d-dimensional vector a and a 1d value b. so a_array contains
												   l * m * d values totally and b_array contains l *m values */

    public float		U;								/* each dimension of the hash space has domain [-U/2, U/2] */
    public float		w;

//	LSBtreePtr *trees;							/* lsbtrees */

    RecContext recContext;

    public LSB() {
        t = -1;
        d = -1;
        n = -1;
        m = -1;
        L = -1;
        B = -1;
        w = -1;
        f = -1;
        U = -1;
        pz = -1;
        ratio = -1;
        quiet = 0;

        a_array = null;
        b_array = null;

        emergency = false;
    }

    public LSB(RecContext recContext) {
        this();
        this.recContext = recContext;
        recContext.setLsb(this);
    }

    /*    //--=== internal ===--
    virtual void	freadNextEntry		(FILE *_fp, int * _son, int * _key);
    virtual void	gen_vectors			();
    virtual void	getHashVector		(int _tableID, float *_key, float *_g);
    virtual void	getHashPara			(int _u, int _v, float **_a_vector, float *_b);
    virtual float	get1HashV			(int _u, int _v, float *_key);
    virtual int		getZ				(float *_g, int *_z);
    virtual	int		getLowestCommonLevel(int *_z1, int *_z2);
    virtual int		get_m				(int _r, float _w, int _n, int _B, int _d);
    virtual double	get_rho				(int _r, float _w);
    virtual int		get_obj_size		(int _dim);
    virtual int		get_u				();
    virtual int		insert				(int _treeID, int _son, float * _key);
    virtual int		readParaFile		(char *_fname);
    virtual int		writeParaFile		(char *_fname);

    //--=== external ===--
    virtual int		buildFromFile		(char *_dsPath, char *_forestPath);
    virtual int		bulkload			(char *_dspath, char *_forestPath);
    virtual void	init				(int _t, int _d, int _n, int _B, int _L, int _ratio);
//	virtual int		knn					(int *_q, int _k, LSB_Hentry *_rslt, int _numTrees);*/



    public int getLowestCommonLevel(int[]_z1, int[]_z2)
    {
        int ret		= 0;

        int	c		= -1;
        int	i		= -1;
        int	j		= -1;
        int	mask	= -1;

        c = u * m;

        for (i = 0; i < pz; i++)
        {
            mask = 1 << 30;

            for (j = 0; j < 31 && c > 0; j++)
            {
                if ((_z1[i] & mask) == (_z2[i] & mask))
                {
                    ret++;
                    c--;
                    mask >>= 1;
                }
                else
                {
                    i = pz; j = 32;
                }
            }
        }

        ret /= m;
        ret = u - ret;

        return ret;
    }


    public int get_m(int _r, float _w, int _n, int _B, int _d)
    {
        int				ret = -1;

        double	PI	= 3.14159265;
        double			p2	= 1;

        p2 -= 2 * new Rand().normal_cdf(-_w/_r, (float) 0.001);
        p2 -= (2 * _r / (Math.sqrt(2*PI) * _w)) * (1 - Math.exp(-_w*_w / (2*_r*_r)));

        ret = (int) Math.ceil( Math.log( ((double) _n) * _d/_B) / Math.log(1.0/p2));

        return ret;
    }

    public double get_rho(int _r, float _w)
    {
        double ret = -1;

        double PI = 3.14159265;

        double p1 = 1;
        double p2 = 1;

        p2 -= 2 * new Rand().normal_cdf(-_w / _r, (float) 0.001);
        p2 -= (2 * _r / (Math.sqrt(2 * PI) * _w)) * (1 - Math.exp(-_w * _w / (2 * _r * _r)));

        p1 -= 2 * new Rand().normal_cdf(-_w / 1, (float) 0.001);
        p1 -= (2 * 1 / (Math.sqrt(2 * PI) * _w)) * (1 - Math.exp(-_w * _w / 2));

        ret = Math.log(1 / p1) / Math.log(1 / p2);

        return ret;
    }


    public void init(int _t, int _d, int _n, int _B, int _L, int _ratio)
    {
        t		= _t;
        d		= _d;
        n		= _n;
        B		= _B;
        ratio	= _ratio;

        w		= 4;
        f		= (int) Math.ceil( Math.log((double) d)/Math.log(2.0) + Math.log((double) t)/Math.log(2.0) );
        m		= get_m(ratio, w, n, B, d);


        L	= (int) Math.ceil( Math.pow( (float) n*d/B, (float) 1/ratio) );
        if (_L > 0 && _L < L)
            L = _L;



        gen_vectors();

        u		= get_u();
        if (u > 30)
            System.out.printf("u too large\n");
        //error("u too large\n", true);

        pz		= (int) Math.ceil( ((double) (u * m)) / 31 );

        U = (1 << u) * w;

        if (quiet <= 9)
        {
            System.out.printf("Parameters:\n");
            System.out.printf("\tm = %d\n", m);
            System.out.printf("\tl = %d\n", L);
            System.out.printf("\tu = %d\n", u);
            System.out.printf("\tU = %.1f\n\n", U);

            //////////output a_array and b_array//////added by emily on Feb.8//////

            System.out.printf("B = %d\n", B);
            System.out.printf("n = %d\n", n);
            System.out.printf("d = %d\n", d);
            System.out.printf("t = %d\n", t);
            System.out.printf("ratio = %d\n", ratio);
            System.out.printf("l = %d\n", L);

            int		acnt = 0;
            int		bcnt = 0;
            for (int i = 0; i < L; i++)
            {
                for (int j = 0; j < m; j++)
                {
                    for (int k = 0; k < d; k++)
                    {
                        System.out.printf("%f\t", a_array[acnt]);

                        acnt++;
                    }

                    System.out.printf("\n");

                    System.out.printf("%f\n", b_array[bcnt]);

                    bcnt++;

                    System.out.printf("\n");
                }
            }
            ////////////////////////
        }
    }


    public int readParaFile(String _fname)
    {
        int		ret		= 0;

        File	fp		= null;
        int		acnt	= 0;
        int		bcnt	= 0;
        int		i		= -1;
        int		j		= -1;
        int		k		= -1;

        fp = new File(_fname);

        if (!fp.exists())
        {
            System.out.printf("Could not open %s.\n", _fname);

            ret = 1;
            System.exit(-1);
            //// TODO: 2020/2/24
        }


        // this would never be used , as we use scanner to read  int and float  numeric
        BufferedReader br = null;
        Scanner scanner;
        try {
            br = new BufferedReader(new FileReader(fp));
            scanner = new Scanner(fp);
        }catch (Exception e){
            throw new RuntimeException(e);
        }


        dsPath = scanner.next();
        scanner.next();
        scanner.next();
        m = scanner.nextInt();
        scanner.next();
        scanner.next();
        L = scanner.nextInt();
        scanner.next();
        scanner.next();
        u = scanner.nextInt();
        scanner.next();
        scanner.next();
        U = scanner.nextFloat();
        scanner.next();
        scanner.next();
        B = scanner.nextInt();
        scanner.next();
        scanner.next();
        n = scanner.nextInt();
        scanner.next();
        scanner.next();
        d = scanner.nextInt();
        scanner.next();
        scanner.next();
        t = scanner.nextInt();
        scanner.next();
        scanner.next();
        ratio = scanner.nextInt();
        scanner.next();
        scanner.next();
        L = scanner.nextInt();




     /*
     Map<String,String> kvs = readLineValue(br);
       m = string2Int(kvs.get("m"));
        L = string2Int(kvs.get("l"));
        u = string2Int(kvs.get("u"));
        U = string2Float(kvs.get("U"));
        ////////////////////////
        B = string2Int(kvs.get("B"));
        n = string2Int(kvs.get("n"));
        d= string2Int(kvs.get("d"));
        t = string2Int(kvs.get("t"));
        ratio = string2Int(kvs.get("ratio"));
        l = string2Int(kvs.get("L"));*/


        w	= 4;
        f	= (int) Math.ceil( Math.log((double) d)/Math.log(2.0) + Math.log((double) t)/Math.log(2.0) );

        m = get_m(ratio, w, n, B, d);

        a_array = new float[L * m * d];
        b_array = new float[L * m];

        acnt = 0;
        bcnt = 0;

        

        for (i = 0; i < L; i ++)
        {
            for (j = 0; j < m; j ++)
            {
                for (k = 0; k < d; k ++)
                {
                    a_array[acnt] = scanner.nextFloat();

                    acnt++;
                }

                b_array[bcnt] =  scanner.nextFloat();

                bcnt++;

            }
        }

        u = get_u();

        pz = (int) Math.ceil( ((double) (u * m)) / 31 );

        U = (1 << u) * w;


        if (br != null){
            fclose(br);
            scanner.close();
        }


        return ret;
    }



    public void getHashVector(int _tableID, float[]_key, float[]_g)
    {
        int i;

        for (i = 0; i < m; i ++)
        {
            _g[i] = get1HashV(_tableID, i, _key);
        }
    }


    public float get1HashV(int _u, int _v, float[]_key)
    {
        float	ret			= 0;

        double	b = 0f;
        int		i;

        Object[] res = getHashPara(_u, _v);
        int posBase = (int)res[0];
        b = (double)res[1];

        for (i = 0; i < d; i ++)
        {
            ret +=  a_array[i +posBase] * _key[i];
        }

        ret += b;

        return ret;
    }


    public int  getZ(float[]_g, int[]_z)
    {
        int ret = 0;

        //charptr	* bmp		= NULL;
        int		c = -1;
        int		cc = -1;
        int		numCell_Dim = -1;
        int[]		g = null;
        int		i = -1;
        int		j = -1;
        int		mask = -1;
        int		v = -1;

    /* === codes to be deleted ===
    bmap = new charptr[m];
    for (i = 0; i < m; i ++)
        bmap[i] = new char[u];
    */

        numCell_Dim = 1 << u;

        g = new int[m];

        for (i = 0; i < m; i++)
        {
            g[i] = (int)((_g[i] + U / 2) / w);

            if (g[i] < 0 || g[i] >= numCell_Dim)
            {
                System.out.printf("Illegal coordinate in the hash space found.\n");

                ret = 1;
                //// TODO: 2020/2/24
                System.exit(-1);
            }
        }

    /*
    //-- + --
    for (i = 0; i < m; i ++)
    {
        printINT_in_BIN(g[i], u);
    }
    printf("\n");
    //-- + --
    */
        c = 0;
        cc = 0;
        v = 0;

        for (i = u - 1; i >= 0; i--)
        {
            mask = 1 << i;

            for (j = 0; j < m; j++)
            {
                v <<= 1;

                if ((g[j] & mask) == 0)
                    v++;

                c++;

                if (c == 31)
                {
                    _z[cc] = v;
                    cc++;

                    c = 0;
                    v = 0;
                }
            }
        }

        if (c != 31)
        {
            v <<= (31 - c);
            _z[cc] = v;
            cc++;
        }

        if (cc != pz || c != (u * m - (u * m / 31 * 31)))
        {
            System.out.println("Error in LSB::getZ().\n");

//            goto recycle;
            //// TODO: 2020/2/24
            System.exit(-1);
        }

        return ret;
    }


    public  void gen_vectors()
    {
        int		i		= -1;
        float	max_b	= (float) Math.pow(2.0, f) * w * w;

        a_array = new float[L * m * d];
        for (i = 0; i < L * m * d; i ++)
            a_array[i] = new Rand().gaussian(0, 1);

        b_array = new float[L * m];
        for (i = 0; i < L * m; i ++)
            b_array[i] = new Rand().new_uniform(0, max_b);
    }



    public int   get_u()
    {
        int		ret			= -1;

        int		i			= -1;
        int		j			= -1;
        float	absSum		= -1;

        double b			= -1;
        double maxV		= -1;
        double thisV		= -1;

        maxV = (float) Math.pow(2.0, f);

        for (i = 0; i < L; i ++)
        {
            for (j = 0; j < m; j ++)
            {

                Object[] res =  getHashPara(i, j);
                int posBase = (int)res[0];
                b = (double)res[1];

                absSum = 0;
                for (u = 0; u < d; u ++)
                {
                    absSum += (float) fabs(a_array[u + posBase]);
                }

                thisV = 2 * (absSum * t + b) / w;
                if (maxV < thisV)
                    maxV = thisV;
            }
        }

        ret = (int) Math.ceil(Math.log((double) maxV) / Math.log(2.0) - 1) + 1;

        return ret;
    }

    public Object[] getHashPara(int _u, int _v)
    {
        int a =  _u * (m * d) + _v * d;
        double b = b_array[_u * m + _v];
        return new Object[]{a,b};
    }


    public int get_obj_size(int _dim)
    {
        int ret =  4 + 4 + _dim * 4;

        //for id, z-order value, and _dim coordinates
        return ret;
    }


    public int insert(int _treeID, int _son, float[] _key)
    {
        int		ret		= 0;

//	B_Entry	* e		= NULL;
        float[]	 g		=  null;
        int[]		z		= null;

        g = new float[m];
        z = new int[pz];

        getHashVector(_treeID, _key, g);
        if (getZ(g, z) == 0)
        {
            ret = 1;
            System.exit(-1);
            //// TODO: 2020/2/24
        }




        return ret;
    }



    public int writeParaFile(String _fname)
    {
        int		ret			= 0;

        //int		i			= -1;
        //int		j			= -1;
        int		u			= -1;
        File  fp		= null;
        float[] aVector	= null;
        double	b			= -1;
      

        System.out.printf("%s\n", _fname);
        System.out.printf("%s\n", forestPath);



        fp = new File(_fname);

        FileWriter fw =null;
        BufferedWriter bw = null;
        if (!fp.exists())
        {
            System.out.printf("I could not create %s.\n", _fname);
            System.out.printf("Perhaps no such folder %s?\n", forestPath);

            ret = 1;

            System.exit(-1);
            //// TODO: 2020/2/24
        }else{
            try {
                fw = new FileWriter(fp);
                bw = new BufferedWriter((fw));
            }catch (Exception e){
                throw  new RuntimeException(e);
            }
        }

        System.out.println("open the file successfully");


        fprintf(bw, "%s\n", dsPath);
        fprintf(bw, "B = %d\n", B);
        fprintf(bw, "n = %d\n", n);
        fprintf(bw, "d = %d\n", d);
        fprintf(bw, "t = %d\n", t);
        fprintf(bw, "ratio = %d\n", ratio);
        fprintf(bw, "l = %d\n", L);

        System.out.printf("L=%d \n", L);
        L = 2;
        m = 3;
        for (int i = 0; i < L; i++)
        {
            for (int j = 0; j < m; j++)
            {
                Object[] res = getHashPara(i,j);
                int posBase = (int)res[0];
                b = (double)res[1];

                fprintf(bw, "%f", a_array[posBase]);
                for (u = 1; u < d; u ++)
                {
                    fprintf(bw, " %f", a_array[posBase + u]);
                }
                fprintf(bw, "\n");

                fprintf(bw, "%f\n", b);

            }
        }

        recycle:
        if (bw != null)
            fclose(bw);

        return ret;
    }

    private void fprintf(BufferedWriter bw, String format , Object o){

        try {
            bw.newLine();
            bw.write(String.format(format, o));
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }


    private void fprintf(BufferedWriter bw, String format ){

        try {
            bw.write(String.format(format, ""));
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }

    private void fclose(BufferedWriter bw){
        try {
            bw.close();
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }

    private void fclose(BufferedReader br){
        try {
            br.close();
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }

    private float fabs(float a){
        return a<0? -a:a;
    }



    private String readFirstLineValue(BufferedReader br){
        String line;
        try {
            line = br.readLine();
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }

        return line;
    }

    private Map<String,String> readLineValue(BufferedReader br){
        Map<String,String> res = new HashMap<>();
        String line;
        try {
            while((line = br.readLine()) !=null){
                String[] array = line.split("=");
                if (array!=null && array.length >2){
                    res.put(array[0],array[1]);
                }
            }
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }

        return res;
    }


    int string2Int(String in){
        return Integer.parseInt(in);
    }

    float string2Float(String in){
        return Float.parseFloat(in);
    }

}
