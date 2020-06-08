package tsml.classifiers.distance_based.distances.transformed;
/*

Purpose: // todo - docs - type the purpose of the code here

Contributors: goastler
    
*/

import java.util.function.Function;

import tsml.classifiers.distance_based.distances.DistanceMeasureable;
import tsml.classifiers.distance_based.utils.params.ParamHandler;
import tsml.classifiers.distance_based.utils.params.ParamSet;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.filters.Filter;

public class TransformDistanceMeasure extends TransformedDistanceMeasure implements TransformDistanceMeasureable {

    public TransformDistanceMeasure(String name,
        Filter transformer, DistanceFunction distanceFunction) {
        super(name, transformer, distanceFunction);
    }

    @Override
    public void setDistanceFunction(DistanceFunction distanceFunction) {
        super.setDistanceFunction(distanceFunction);
    }

    @Override
    public void setTransformer(Filter transformer) {
        super.setTransformer(transformer);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override public void setParams(final ParamSet param) {
        super.setParams(param);
        ParamHandler.setParam(param, TRANSFORMER_FLAG, this::setTransformer, Filter.class);
        ParamHandler.setParam(param, DistanceMeasureable.DISTANCE_MEASURE_FLAG, this::setDistanceFunction,
                              DistanceFunction.class);
    }

}
