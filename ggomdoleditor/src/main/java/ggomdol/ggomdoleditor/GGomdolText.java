package ggomdol.ggomdoleditor;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

public class GGomdolText {

    public  final int DEFAULT_TEXT_COLOR;
    public final int DEFAULT_TEXT_SIZE;
	private int mTextColor;
    private int mTextSize;
	private int mTypeface = Typeface.NORMAL;
	private boolean mIsUnderLine = false;

    public ForegroundColorSpan makeColorSpan(int color) {

        if(color == DEFAULT_TEXT_COLOR) {
            return new ForegroundColorSpan(color);
        }

        return new NotBlackForegroundColorSpan(color);
    }

    public class NotBlackForegroundColorSpan extends ForegroundColorSpan {
        public NotBlackForegroundColorSpan(int color) {
            super(color);
        }
    }

    public AbsoluteSizeSpan makeTextSizeSpan(int textSize) {

        if(textSize == DEFAULT_TEXT_SIZE) {
            return new AbsoluteSizeSpan(textSize, true);
        }

        return new NotNormalSizeSpan(textSize);
    }

    public class NotNormalSizeSpan extends AbsoluteSizeSpan {
        public NotNormalSizeSpan(int textPropotionSize) {
            super(textPropotionSize, true);
        }
    }

	public GGomdolText(int baseTextColor, int baseTextSize) {

        DEFAULT_TEXT_COLOR = baseTextColor;
        DEFAULT_TEXT_SIZE = baseTextSize;

		mTextColor = DEFAULT_TEXT_COLOR;
        mTextSize = DEFAULT_TEXT_SIZE;
	}
	
    public int getTextColor() {
        return mTextColor;
    }

    public boolean isBold() {
    	
    	if ((mTypeface & Typeface.BOLD) == Typeface.BOLD) {
			return true;
		}
    	return false;
    }

    public boolean isItalic() {

        if ((mTypeface & Typeface.ITALIC) == Typeface.ITALIC) {
            return true;
        }
        return false;
    }

    public boolean isUnderLine() {
        return mIsUnderLine;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public void setBold(boolean isBold) {

        if(isBold == true) {
            mTypeface |= Typeface.BOLD;
        } else {
            mTypeface &= ~Typeface.BOLD;
        }
    }

    public void setItalic(boolean isItalic) {
    	
    	if(isItalic == true) {
    		mTypeface |= Typeface.ITALIC;
    	} else {
    		mTypeface &= ~Typeface.ITALIC;
    	}
    }
    
    public void setUnderLine(boolean isUnderLine) {

        mIsUnderLine = isUnderLine;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void onTextChangedSpanProcess(Editable editable, int start, int count) {
    	
   		addSpan(editable, start, count, ForegroundColorSpan.class);
   		addSpan(editable, start, count, StyleSpan.class);
   		addSpan(editable, start, count, RealUnderlineSpan.class);
        addSpan(editable, start, count, AbsoluteSizeSpan.class);
    }
    
	private void addSpan(Editable editable, int start, int count, Class classType) {

        int objectSpansSize = 0;
        boolean isCombineSpan = false;
        Object newSpan = null;
        CharacterStyle prevSpan = null;
        
        if(classType == ForegroundColorSpan.class || classType == AbsoluteSizeSpan.class) {
            CharacterStyle objectSpans[] =  (CharacterStyle[])editable.getSpans(0, start, classType);
            objectSpansSize = objectSpans.length;
            if (objectSpansSize > 0) {
                for (int i = 0; i < objectSpans.length; i++) {
                    if (editable.getSpanEnd(objectSpans[i]) == start) {
                        prevSpan = objectSpans[i];
                        break;
                    }
                    prevSpan = objectSpans[i];
                }
            }

            if(classType == ForegroundColorSpan.class) {
                newSpan = makeColorSpan(mTextColor);
                isCombineSpan = prevSpan != null && ((ForegroundColorSpan)prevSpan).getForegroundColor() == mTextColor;
            } else if(classType == AbsoluteSizeSpan.class) {
                newSpan = makeTextSizeSpan(mTextSize);
                isCombineSpan = prevSpan != null && ((AbsoluteSizeSpan)prevSpan).getSize() == mTextSize;
            }
        } else if(classType == StyleSpan.class) {
            CharacterStyle objectSpans[] =  (CharacterStyle[])editable.getSpans(0, start, CharacterStyle.class);
            objectSpansSize = objectSpans.length;
            if (objectSpansSize > 0) {
                for (int i = 0; i < objectSpans.length; i++) {
                    if (objectSpans[i].getClass() == StyleSpan.class || objectSpans[i].getClass() == RealUnderlineSpan.class) {
                        if (editable.getSpanEnd(objectSpans[i]) == start) {
                            prevSpan = objectSpans[i];
                            break;
                        } else if(editable.getSpanStart(objectSpans[i]) < start && start <editable.getSpanEnd(objectSpans[i])) {
                            prevSpan = objectSpans[i];
                            break;
                        }
                    }
                }
            }
            if (mTypeface == Typeface.NORMAL) {
                newSpan = null;
            } else {
                newSpan = new StyleSpan(mTypeface);
            }
            isCombineSpan = prevSpan != null && prevSpan.getClass() == StyleSpan.class && ((StyleSpan)prevSpan).getStyle() == mTypeface;
        } else if(classType == RealUnderlineSpan.class) {
            CharacterStyle objectSpans[] =  (CharacterStyle[])editable.getSpans(0, start, CharacterStyle.class);
            objectSpansSize = objectSpans.length;
            if (objectSpansSize > 0) {
                for (int i = 0; i < objectSpans.length; i++) {
                    if (objectSpans[i].getClass() == StyleSpan.class || objectSpans[i].getClass() == RealUnderlineSpan.class) {
                        if (editable.getSpanEnd(objectSpans[i]) == start) {
                            prevSpan = objectSpans[i];
                            break;
                        } else if(editable.getSpanStart(objectSpans[i]) < start && start <editable.getSpanEnd(objectSpans[i])) {
                            prevSpan = objectSpans[i];
                            break;
                        }
                    }
                }
            }
            
            if (mIsUnderLine) {
                newSpan = new RealUnderlineSpan();
            } else {
                newSpan = new RemovedUnderlineSpan();
            }
            isCombineSpan = prevSpan != null && prevSpan.getClass() == newSpan.getClass();
        }
    	applySpanInEditText(editable, newSpan, prevSpan, start, start + count, isCombineSpan);
    }
    
    private void applySpanInEditText(Editable editable, Object addNewSpan, CharacterStyle prevSpan, int startSelection, int endSelection, boolean isCombineSpan) {
        if (prevSpan == null || isCombineSpan == false) {
            if (prevSpan != null) {
                int prevStart = editable.getSpanStart(prevSpan);
                int prevEnd = editable.getSpanEnd(prevSpan);
                if (prevSpan instanceof ForegroundColorSpan && prevStart <startSelection && prevEnd > endSelection) {
                    int foregroundColor = ((ForegroundColorSpan)prevSpan).getForegroundColor();
                    editable.setSpan(makeColorSpan(foregroundColor), prevStart, startSelection, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editable.setSpan(makeColorSpan(foregroundColor), endSelection, prevEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editable.removeSpan(prevSpan);
                } else if(prevSpan instanceof AbsoluteSizeSpan && prevStart <startSelection && prevEnd > endSelection) {
                    int textSize = ((AbsoluteSizeSpan)prevSpan).getSize();
                    editable.setSpan(makeTextSizeSpan(textSize), prevStart, startSelection, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editable.setSpan(makeTextSizeSpan(textSize), endSelection, prevEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editable.removeSpan(prevSpan);
                } else if (prevStart <startSelection && prevEnd > endSelection) {
                    if (prevSpan.getClass() == StyleSpan.class){
                        int typeFace = ((StyleSpan)prevSpan).getStyle();
                        editable.setSpan(new StyleSpan(typeFace), prevStart, startSelection, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        editable.setSpan(new StyleSpan(typeFace), endSelection, prevEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        editable.removeSpan(prevSpan);
                    } else if (prevSpan.getClass() == RealUnderlineSpan.class) {
                        editable.setSpan(new RealUnderlineSpan(), prevStart, startSelection, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        editable.setSpan(new RealUnderlineSpan(), endSelection, prevEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        editable.removeSpan(prevSpan);
                    }
                }
            }
            editable.setSpan(addNewSpan, startSelection, endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            int prevStart = editable.getSpanStart(prevSpan);
            int prevEnd = editable.getSpanEnd(prevSpan);

            if (prevEnd <= endSelection) {
                editable.setSpan(addNewSpan, prevStart, endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                int next = editable.nextSpanTransition(endSelection, prevEnd, prevSpan.getClass());
                if (next != prevEnd) {
                    editable.setSpan(addNewSpan, prevStart, next, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    editable.setSpan(addNewSpan, prevStart, prevEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            editable.removeSpan(prevSpan);
        }
    }

    public static class RealUnderlineSpan extends UnderlineSpan {}
    public static class RemovedUnderlineSpan {}
}
