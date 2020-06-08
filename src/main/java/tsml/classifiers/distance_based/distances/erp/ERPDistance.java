package tsml.classifiers.distance_based.distances.erp;

import tsml.classifiers.distance_based.distances.BaseDistanceMeasure;
import tsml.classifiers.distance_based.utils.instance.ExposedDenseInstance;
import tsml.classifiers.distance_based.utils.params.ParamHandler;
import tsml.classifiers.distance_based.utils.params.ParamSet;
import weka.core.Instance;

/**
 * ERP distance measure.
 * <p>
 * Contributors: goastler
 */
public class ERPDistance extends BaseDistanceMeasure {

    private double penalty = 0;
    private int windowSize = 0;
    private double[][] matrix;
    private boolean keepMatrix = false;

    public static final String PENALTY_FLAG = "p";

    public static final String WINDOW_SIZE_FLAG = "w";

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    public double[][] getMatrix() {
        return matrix;
    }
    @Override
    public double distance(final Instance ai,
        final Instance bi,
        final double limit) {

        checkData(ai, bi);

        double[] a = ExposedDenseInstance.extractAttributeValuesAndClassLabel(ai);
        double[] b = ExposedDenseInstance.extractAttributeValuesAndClassLabel(bi);

        int aLength = a.length - 1;
        int bLength = b.length - 1;

        // put a or first as the longest time series
        if(bLength > aLength) {
            double[] tmp = a;
            a = b;
            b = tmp;
            int tmpLength = aLength;
            aLength = bLength;
            bLength = tmpLength;
        }

        // Current and previous columns of the matrix
        double[] row = new double[bLength];
        double[] prevRow = new double[bLength];
        if(keepMatrix) {
            matrix = new double[aLength][bLength];
        }

        // size of edit distance band
        // bandsize is the maximum allowed distance to the diagonal
        int windowSize = this.windowSize;
        if(windowSize < 0) {
            windowSize = aLength - 1;
        }

        boolean tooBig = true;
        int start = 1;
        int end = windowSize;
        if(end + 1 < bLength) {
            row[end + 1] = Double.POSITIVE_INFINITY;
        }
        row[0] = 0; // top left cell of matrix is always 0

        // populate first row
        for(int j = start; j <= end; j++) {
            final double cost = row[j - 1] + Math.pow(b[j] - penalty, 2);
            row[j] = cost;
            if(tooBig && cost < limit) {
                tooBig = false;
            }
        }
        // populate matrix
        if(keepMatrix) {
            System.arraycopy(row, 0, matrix[0], 0, row.length);
        }
        // Swap current and prevRow arrays. We'll just overwrite the new row.
        {
            double[] temp = prevRow;
            prevRow = row;
            row = temp;
        }
        // check if the limit has been hit IFF first row was populated
        if(windowSize > 0 && tooBig) {
            return Double.POSITIVE_INFINITY;
        }
        // populate remaining rows
        for(int i = 1; i < aLength; i++) {
            tooBig = true;
            // start and end of window
            start = Math.max(0, i - windowSize);
            end = Math.min(bLength - 1, i + windowSize);
            // must set the value before and after the window to inf if available as the following row will use these
            // in top / left / top-left comparisons
            if(start - 1 >= 0) {
                row[start - 1] = Double.POSITIVE_INFINITY;
            }
            if(end + 1 < bLength) {
                row[end + 1] = Double.POSITIVE_INFINITY;
            }
            // when l == 0 neither left nor top left can be picked, therefore it must use top
            if(start == 0) {
                row[start] = prevRow[start] + Math.pow(a[i] - penalty, 2);
                start++;
            }
            for(int j = start; j <= end; j++) {
                // compute squared distance of feature vectors
                final double v1 = a[i];
                final double v2 = b[j];
                final double leftPenalty = Math.pow(v1 - penalty, 2);
                final double topPenalty = Math.pow(v2 - penalty, 2);
                final double topLeftPenalty = Math.pow(v1 - v2, 2);
                final double topLeft = prevRow[j - 1] + topLeftPenalty;
                final double left = row[j - 1] + topPenalty;
                final double top = prevRow[j] + leftPenalty;
                final double cost;

                if(topLeft > left && left < top) {
                    // del
                    cost = left;
                } else if(topLeft > top && top < left) {
                    // ins
                    cost = top;
                } else {
                    // match
                    cost = topLeft;
                }

                row[j] = cost;

                if(tooBig && cost < limit) {
                    tooBig = false;
                }
            }
            if(keepMatrix) {
                System.arraycopy(row, 0, matrix[i], 0, row.length);
            }
            if(tooBig) {
                return Double.POSITIVE_INFINITY;
            }
            // Swap current and prevRow arrays. We'll just overwrite the new row.
            {
                double[] temp = prevRow;
                prevRow = row;
                row = temp;
            }
        }

        return prevRow[bLength - 1];
    }

    @Override
    public ParamSet getParams() {
        return super.getParams().add(PENALTY_FLAG, penalty).add(WINDOW_SIZE_FLAG, windowSize);
    }

    @Override
    public void setParams(final ParamSet param) {
        ParamHandler.setParam(param, PENALTY_FLAG, this::setPenalty, Double.class);
        ParamHandler.setParam(param, WINDOW_SIZE_FLAG, this::setWindowSize, Integer.class);
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(final int windowSize) {
        this.windowSize = windowSize;
    }

    public boolean isKeepMatrix() {
        return keepMatrix;
    }

    public void setKeepMatrix(final boolean keepMatrix) {
        this.keepMatrix = keepMatrix;
    }
}
