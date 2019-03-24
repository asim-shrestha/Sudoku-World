package com.sigma.sudokuworld.sudoku;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SudokuGridView extends View {
    private int mBoardLength = 9;
    private int mSubsectionHeight = 3;
    private int mSubsectionWidth = 3;

    private int mXOrigin;
    private int mYOrigin;
    private int mSquareWidth;
    private int mSquareHeight;
    private int mCellWidth;
    private int mCellHeight;

    private Rect mGridBoundingRect;

    private Paint mGridPaint;
    private Paint mBoldPaint;
    private Paint mCellFilledPaint;
    private Paint mLockedCellFillPaint;
    private Paint mIncorrectCellFillPaint;
    private Paint mTextPaint;

    private Float mTextPaintTextHeight;

    private Boolean mRectangleCells;
    private String[] mCellLabels;       //Labels for every cell in grid
    private int mIncorrectCell = -1;    //Points to first incorrect cell to highlight. -1 = no cell
    private int mHighlightedCell = -1;  //Points to cell to draw highlight in. -1 = no cell

    public SudokuGridView(Context context) {
        this(context, null);
    }

    public SudokuGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint(context, attrs);

        mCellLabels = new String[mBoardLength*mBoardLength];
        mRectangleCells = false;
        Arrays.fill(mCellLabels, "");
    }

    public void setGridSize(int boardLength) {
        mBoardLength = boardLength;
        mSubsectionHeight = (int) Math.floor(Math.sqrt(boardLength));
        mSubsectionWidth = (int) Math.ceil(Math.sqrt(boardLength));
    }

    private void initPaint(Context context, AttributeSet attrs) {
        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCellFilledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLockedCellFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIncorrectCellFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        //Get styling from activity_sudoku.xml
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SudokuGridView,
                0, 0);

        //Apply styling from xml file, if not styling set default values
        try {
            mGridPaint.setStrokeWidth(a.getDimension(R.styleable.SudokuGridView_gridPaintStrokeWidth, 5));
            mGridPaint.setColor(a.getColor(R.styleable.SudokuGridView_gridPaintColor, Color.BLACK));
            mGridPaint.setAlpha(75);
            mBoldPaint.setStrokeWidth(a.getDimension(R.styleable.SudokuGridView_boldGridPaintStrokeWidth, 10));
            mBoldPaint.setColor(a.getColor(R.styleable.SudokuGridView_boldGridPaintColor, Color.BLACK));
            mBoldPaint.setAlpha(125);
            mCellFilledPaint.setColor(a.getColor(R.styleable.SudokuGridView_highlightedCellColour, Color.YELLOW));
            mLockedCellFillPaint.setColor(a.getColor(R.styleable.SudokuGridView_lockedCellColour, Color.GRAY));
            mIncorrectCellFillPaint.setColor(a.getColor(R.styleable.SudokuGridView_incorrectCellColour, Color.RED));
        } finally {
            a.recycle();
        }

        //More paint styling
        mGridPaint.setStrokeCap(Paint.Cap.BUTT);
        mBoldPaint.setStrokeCap(Paint.Cap.ROUND);
        mCellFilledPaint.setStyle(Paint.Style.FILL);
        mLockedCellFillPaint.setStyle(Paint.Style.FILL);
        mIncorrectCellFillPaint.setStyle(Paint.Style.FILL);
    }

    public void setCellLabels(LifecycleOwner owner, LiveData<List<String>> labelsLiveData) {
        final Observer<List<String>> cellLabelsObserver = new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> strings) {
                mCellLabels = strings.toArray(new String[0]);
                invalidate();
            }
        };
        labelsLiveData.observe(owner, cellLabelsObserver);
    }

    public void lazySetLockedCellsLabels(boolean[] lockedCells) {
        for (int i = 0; i < lockedCells.length; i++) {
            if (lockedCells[i]) mCellLabels[i] = Character.toString(KeyConstants.CELL_LOCKED_FLAG);
        }
        invalidate();
    }

    @Override   //This is for accessibility (REQUIRED BY ANDROID STUDIO)
    public boolean performClick() {
        return super.performClick();
    }

    /**
     *  Calculates the correct size for all objects to be drawn within the view.
     *  Gets called when the view size changes.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mXOrigin = getPaddingLeft();
        mYOrigin = getPaddingTop();

        int xPad = getPaddingLeft() + getPaddingRight();
        int yPad = getPaddingTop() + getPaddingBottom();
        int maxPad = Math.max(xPad, yPad);

        if (!mRectangleCells) {
            int squareSize = Math.min(w - maxPad, h - maxPad);
            int cellSize = squareSize / mBoardLength;
            squareSize = cellSize * mBoardLength; //Guard against for floating point errors
            mCellHeight = cellSize;
            mCellWidth = cellSize;
            mSquareHeight = squareSize;
            mSquareWidth = squareSize;
        } else {
            mSquareWidth = (w - maxPad);
            mCellWidth = mSquareWidth / mBoardLength;
            mSquareHeight = (h - maxPad);
            mCellHeight = mSquareHeight / mBoardLength;
        }

        mGridBoundingRect = new Rect(
                mXOrigin,
                mYOrigin,
                mXOrigin + mSquareWidth,
                mYOrigin + mSquareHeight
        );

        mTextPaint.setTextSize(mCellWidth / 2f);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextPaintTextHeight = fontMetrics.descent - fontMetrics.ascent;
    }

    /**
     * Tell the layout manager the preferred size for the view.
     * Makes the view square.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!mRectangleCells) {
            int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
            setMeasuredDimension(size, size);
        } else {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawCellFill(canvas);
        drawGrid(canvas);
        highlightNeighbours(canvas);
    }

    /**
     * Draws the the sudoku grid
     * @param canvas canvas
     */
    private void drawGrid(Canvas canvas) {

        //Horizontal Lines
        for(int i = 0; i <= mBoardLength; i++) {

            //Either bold or not bold
            if (i % mSubsectionHeight == 0) {
                canvas.drawLine(
                        mXOrigin,mYOrigin + (i * mCellHeight),
                        mSquareWidth + mXOrigin, mYOrigin + (i * mCellHeight),
                        mBoldPaint);
            } else {
                canvas.drawLine(
                        mXOrigin,mYOrigin + (i * mCellHeight),
                        mSquareWidth + mXOrigin, mYOrigin + (i * mCellHeight),
                        mGridPaint);
            }
        }

        //Vertical Lines
        for(int i = 0; i <= mBoardLength; i++) {

            //Either bold or not bold
            if (i % mSubsectionHeight == 0) {
                canvas.drawLine(
                        mXOrigin + (i * mCellWidth), mYOrigin,
                        mXOrigin + (i * mCellWidth), mSquareHeight + mYOrigin,
                        mBoldPaint);
            } else {
                canvas.drawLine(
                        mXOrigin + (i * mCellWidth), mYOrigin,
                        mXOrigin + (i * mCellWidth), mSquareHeight + mYOrigin,
                        mGridPaint);
            }
        }
    }

    /**
     * Draws the content within the cell
     * @param canvas canvas
     */
    private void drawCellFill(Canvas canvas) {
        for (int i = 0; i < mCellLabels.length; i++) {
            int cx = i % mBoardLength;   //x cell pos
            int cy = i / mBoardLength;   //y cell pos

            // If its the cell that's currently INCORRECT, draw its highlight
            if(i == mIncorrectCell) {
                Rect cellRect = new Rect(
                        mXOrigin + (cx * mCellWidth),
                        mYOrigin + (cy * mCellHeight),
                        mXOrigin + ((cx + 1) * mCellWidth),
                        mYOrigin + ((cy + 1) * mCellHeight)
                );

                canvas.drawRect(cellRect, mIncorrectCellFillPaint);
            }

            //If its the cell that's currently highlighted draw the highlight
            else if (i == mHighlightedCell) {
                Rect cellRect = new Rect(
                        mXOrigin + (cx * mCellWidth),
                        mYOrigin + (cy * mCellHeight),
                        mXOrigin + ((cx + 1) * mCellWidth),
                        mYOrigin + ((cy + 1) * mCellHeight)
                );
                canvas.drawRect(cellRect, mCellFilledPaint);
            }

            //If the cell has a label
            String label = mCellLabels[i];
            if (!label.equals("")) {


                //Draws the cell fill for squares that cant be edited
                if (label.charAt(0) == KeyConstants.CELL_LOCKED_FLAG) {

                    Rect cellRect = new Rect(
                            mXOrigin + (cx * mCellWidth),
                            mYOrigin + (cy * mCellHeight),
                            mXOrigin + ((cx + 1) * mCellWidth),
                            mYOrigin + ((cy + 1) * mCellHeight)
                    );

                    canvas.drawRect(cellRect, mLockedCellFillPaint);
                    label = label.substring(1);
                }

                //Measure the width of the label and draw it in the cell
                float textWidth = mTextPaint.measureText(label);

                //Text too big for cell decrease size
                float defaultTextSize = mTextPaint.getTextSize();
                while (textWidth > mCellWidth) {
                    mTextPaint.setTextSize(mTextPaint.getTextSize() - 1);
                    textWidth = mTextPaint.measureText(label);
                }

                canvas.drawText(label,
                        mXOrigin + (cx * mCellWidth) + (mCellWidth / 2f) - (textWidth / 2),
                        mYOrigin + (cy * mCellHeight) + (mCellHeight / 2f) + (mTextPaintTextHeight / 2) - 10,
                        mTextPaint);

                //Reset text paint size
                mTextPaint.setTextSize(defaultTextSize);
            }



        }
    }

    private void highlightNeighbours(Canvas canvas){
        //This method will highlight all the neighbours of the currently selected cell
        //Neighbours refer to cells in the same column, row, and section as the selected cell

        //No cell is highlighted
        if(mHighlightedCell == -1 ) {return;}

        int row = (mHighlightedCell / mBoardLength);
        int column = (mHighlightedCell % mBoardLength);
        int subsectionRow = mBoardLength * mSubsectionHeight * (row / mSubsectionHeight);
        int subsectionColumn = mSubsectionWidth * (column / mSubsectionWidth);
        int i;
        List<Integer> visitedList = new ArrayList<>();
        visitedList.add(mHighlightedCell);

        //Draw row and column highlights
        for(i = 0; i < mBoardLength; i++) {
            //Row
            int cellNumber = mBoardLength * row + i;
            if (cellNumber != mHighlightedCell)
            {
                drawCellHighlight(canvas, cellNumber);
                visitedList.add(cellNumber);
            }


            //Column
            cellNumber = column + i * mBoardLength;
            if (cellNumber != mHighlightedCell)
            {
                drawCellHighlight(canvas, cellNumber);
                visitedList.add(cellNumber);
            }
        }

        //Draw subsection highlights
        for(i = 0; i < mSubsectionHeight; i++){
            for (int j = 0; j < mSubsectionWidth; j++)
            {
                int cellNumber = subsectionRow + mBoardLength * i + subsectionColumn + j;
                if (!visitedList.contains(cellNumber)) {
                    drawCellHighlight(canvas, cellNumber);
                }
            }
        }
    }

    private void drawCellHighlight(Canvas canvas, int cellNumber) {
        //Draws the individual highlight of a cell

        int cx = cellNumber % mBoardLength;   //x cell pos
        int cy = cellNumber / mBoardLength;   //y cell pos

        Rect cellRect = new Rect(
                mXOrigin + (cx * mCellWidth),
                mYOrigin + (cy * mCellHeight),
                mXOrigin + ((cx + 1) * mCellWidth),
                mYOrigin + ((cy + 1) * mCellHeight)
        );
        Paint paint = new Paint(mGridPaint);
        paint.setAlpha(17);
        canvas.drawRect(cellRect, paint);
    }


    public Rect getGridBounds() {
        return mGridBoundingRect;
    }

    /**
     * Returns the cell number that is closest to the coordinate.
     * Used to find out what cell was touched
     * @param x cord
     * @param y cord
     * @return cell number
     */
    public int getCellNumberFromCoordinates(int x, int y) {
        x -= mXOrigin;
        y -= mYOrigin;
        x /= mCellWidth;
        y /= mCellHeight;
        return (y * mBoardLength) + x;
    }

    public int getHighlightedCell() {
        return mHighlightedCell;
    }
    public void setHighlightedCell(int cellNumber) {
        mHighlightedCell = cellNumber;
    }
    public void clearHighlightedCell() {
        mHighlightedCell = -1;
    }
    public void setRectangleMode(boolean isEnabled) {
        mRectangleCells = isEnabled;
        invalidate();
    }
    public int getIncorrectCell(){
        return mIncorrectCell;
    }
    public void setIncorrectCell(int cellNumber) {
        mIncorrectCell = cellNumber;
    }
    public void clearIncorrectCell() {
        mIncorrectCell = -1;
    }
}
