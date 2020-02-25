package org.turningme.theoretics.common.tokenizer;

/**
 * Created by jpliu on 2020/2/23.
 */
public interface ITokenizer {

    void parse(String str, String seperators);

    int count_tokens();
    String next_token();
    void start_scan();

    String token(int i);
}
