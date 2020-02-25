package org.turningme.theoretics.common.tokenizer;

/**
 * Created by jpliu on 2020/2/23.
 */
public class StringTokenizer extends java.util.StringTokenizer implements ITokenizer{


    public StringTokenizer(String str) {
        super(str);
    }

    public StringTokenizer(String str, String delim) {
        super(str, delim);
    }

    public StringTokenizer(String str, String delim, boolean returnDelims) {
        super(str, delim, returnDelims);
    }

    @Override
    public int count_tokens() {
        return countTokens();
    }

    @Override
    public void parse(String str, String seperators) {
        parse(str,seperators);
    }

    @Override
    public String next_token() {
        return nextToken();
    }

    @Override
    public void start_scan() {
    }

    @Override
    public String token(int i) {
        //// TODO: 2020/2/23  this may be wrong in fact
        return nextToken();
    }
}
