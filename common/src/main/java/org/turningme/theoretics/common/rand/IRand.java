package org.turningme.theoretics.common.rand;

/**
 * Created by jpliu on 2020/2/23.
 */
public interface IRand {
    float gaussian(float mean, float sigma);
    float new_uniform(float _min, float _max);
    double normal_pdf(float _x, float _u, float _sigma);
    double normal_cdf(float _x, float _step);
    float uniform(float _min, float _max);
    float zipf(float x1, float x2, double p);
}
