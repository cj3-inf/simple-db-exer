package simpledb.execution;

import simpledb.common.DbException;
import simpledb.common.Type;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.transaction.TransactionAbortedException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;

    private OpIterator child;

    private int afieldNum;

    private int gfieldNum;

    private Aggregator.Op aop;

    private Aggregator aggregator;

    private OpIterator it;

    /**
     * Constructor.
     * <p>
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntegerAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     *
     * @param child  The OpIterator that is feeding us tuples.
     * @param afield The column over which we are computing an aggregate.
     * @param gfield The column over which we are grouping the result, or -1 if
     *               there is no grouping
     * @param aop    The aggregation operator to use
     */
    public Aggregate(OpIterator child, int afield, int gfield, Aggregator.Op aop) {
        // some code goes here
        this.child = child;
        this.afieldNum = afield;
        this.gfieldNum = gfield;
        this.aop = aop;

        Type gfiledtype = gfield == -1 ? null : this.child.getTupleDesc().getFieldType(this.gfieldNum);

        if(this.child.getTupleDesc().getFieldType(this.afieldNum) == Type.INT_TYPE) {
            this.aggregator = new IntegerAggregator(gfieldNum, gfiledtype, afieldNum, aop);
        } else {
            this.aggregator = new StringAggregator(gfieldNum, gfiledtype, afieldNum, aop);
        }
        this.it = this.aggregator.iterator();

    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     * field index in the <b>INPUT</b> tuples. If not, return
     * {@link Aggregator#NO_GROUPING}
     */
    public int groupField() {
        // some code goes here
        if(gfieldNum == -1) {
            return Aggregator.NO_GROUPING;
        }
        return gfieldNum;
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     * of the groupby field in the <b>OUTPUT</b> tuples. If not, return
     * null;
     */
    public String groupFieldName() {
        // some code goes here
        try {
            String fieldName = it.getTupleDesc().getFieldName(this.gfieldNum);
            return it.getTupleDesc().getFieldName(this.gfieldNum);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * @return the aggregate field
     */
    public int aggregateField() {
        // some code goes here
        return this.afieldNum;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     * tuples
     */
    public String aggregateFieldName() {
        // some code goes here
        if(this.gfieldNum != -1) {
            return this.it.getTupleDesc().getFieldName(0);
        } else {
            return this.it.getTupleDesc().getFieldName(1);
        }
    }

    /**
     * @return return the aggregate operator
     */
    public Aggregator.Op aggregateOp() {
        // some code goes here
        return this.aop;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
        return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
            TransactionAbortedException {
        // some code goes here
        this.child.open();
        while(this.child.hasNext()) this.aggregator.mergeTupleIntoGroup(this.child.next());
        this.it.open();
        super.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate. If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        while(it.hasNext()) return it.next();
        return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        this.child.rewind();
        this.it.rewind();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * <p>
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.it.getTupleDesc();
    }

    public void close() {
        // some code goes here
        super.close();
        this.child.close();
        this.it.close();
    }

    @Override
    public OpIterator[] getChildren() {
        // some code goes here
        return new OpIterator[]{this.child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        // some code goes here
        this.child = children[0];
    }

}
