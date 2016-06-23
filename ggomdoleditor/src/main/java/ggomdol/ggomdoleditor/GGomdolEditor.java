package ggomdol.ggomdoleditor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.BaseMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.output.StringBuilderWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import ggomdol.ggomdoleditor.R;

public class GGomdolEditor extends TextView {

//    private static final String BITMAP_UNICODE = "\u115f";
    private static  final String BITMAP_UNICODE = "￼";
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GGOMDOL/";
//    private static final String BACKGROUND_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GGOMDOL/BACKGROUND/";
    private GGomdolText mGGomdolText;

    public enum Type { UNKOWN, IMAGE, VIDEO, AUDIO }

    private int DEFAULT_BACKGROUND[] = {R.drawable.ggomdol_back_01, R.drawable.ggomdol_back_02, R.drawable.ggomdol_back_03,
                                                                        R.drawable.ggomdol_back_04, R.drawable.ggomdol_back_05};

    private class ClickableMovementMethod extends BaseMovementMethod {

        @Override
        public boolean canSelectArbitrarily() {
            return false;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {

            if (event.getActionMasked() == MotionEvent.ACTION_UP) {

                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();
                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ImageSpan[] link = buffer.getSpans(off, off, ImageSpan.class);

                if (link.length > 0) {
                    Rect parentTextViewRect = new Rect();
                    Layout textViewLayout = getLayout();

                    double startOffsetOfClickedText = buffer.getSpanStart(link[0]);
                    double endOffsetOfClickedText = buffer.getSpanEnd(link[0]);
                    double startXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int)startOffsetOfClickedText);
                    double endXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int)endOffsetOfClickedText);

                    int currentLineStartOffset = textViewLayout.getLineForOffset((int)startOffsetOfClickedText);
                    int currentLineEndOffset = textViewLayout.getLineForOffset((int)endOffsetOfClickedText);
                    textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect);
                    int[] parentTextViewLocation = {0,0};

                    double parentTextViewTopAndBottomOffset = (parentTextViewLocation[1] - getScrollY() + getCompoundPaddingTop());
                    parentTextViewRect.top += parentTextViewTopAndBottomOffset;
                    parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;

                    parentTextViewRect.left += (parentTextViewLocation[0] +startXCoordinatesOfClickedText + getCompoundPaddingLeft() - getScrollX());
                    parentTextViewRect.right = (int) (parentTextViewRect.left + endXCoordinatesOfClickedText - startXCoordinatesOfClickedText);

                    if(event.getX() >= parentTextViewRect.left && event.getX() <= parentTextViewRect.right && event.getY() >= parentTextViewRect.top && event.getY() <= parentTextViewRect.bottom) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link[0].getSource()));
                        getContext().startActivity(intent);
                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                }
            }

            return false;
        }

        @Override
        public void initialize(TextView widget, Spannable text) {
            Selection.removeSelection(text);
        }
    }

//    private class GGomdolFilter implements InputFilter {
//
//        @Override
//        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//            return null;
//        }
//    }

    public GGomdolEditor(Context context, AttributeSet attrs) {
        super(context, attrs);

        File file = new File(DATA_PATH);
        if( !file.exists() ) {
            file.mkdirs();
        }

        setMovementMethod(new ClickableMovementMethod());
        setFocusable(true);
        setFocusableInTouchMode(true);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        setSingleLine(false);
        setLongClickable(false);

        mGGomdolText = new GGomdolText(Color.BLACK, 15);
        addTextChangedListener(new GGomdolTextWatcher());

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getContext().getResources().getDisplayMetrics());
        setPadding(padding, padding, padding, padding);
        setBackground(BitmapFactory.decodeResource(getContext().getResources(), DEFAULT_BACKGROUND[0]));
    }

    private void setBackground(Bitmap bitmap) {

        setBackground(new BitmapDrawable(getContext().getResources(), bitmap));
    }

    public void setBackground() {

        final String RESOURCE_ID = "image_iv";
        String[] from = {RESOURCE_ID };
        int[] to = { R.id.image_iv};

        ArrayList<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();

        for (int index = 0; index < DEFAULT_BACKGROUND.length; index++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put(RESOURCE_ID, Integer.toString(DEFAULT_BACKGROUND[index]));
            aList.add(hm);
        }

        SimpleAdapter adapter = new SimpleAdapter(getContext(), aList, R.layout.ggomdol_background_picker, from, to);
        GridView grid = new GridView(getContext());
        grid.setNumColumns(3);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(grid);
        builder.setNegativeButton("CANCEL", null);
        final AlertDialog dialog = builder.create();

        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                setBackground(BitmapFactory.decodeResource(getContext().getResources(), DEFAULT_BACKGROUND[position]));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override public void setOnLongClickListener(OnLongClickListener l) {}
    @Override public boolean isSuggestionsEnabled() {
        return false;
    }

    public void insertMedia(final Uri uri) {

//        String strSpanInnerText = BITMAP_UNICODE + GGomdolUtil.getPathFromUri(getContext(), uri) + BITMAP_UNICODE;
        String strSpanInnerText = uri.toString();

        SpannableString span = new SpannableString(strSpanInnerText);
        ImageSpan multimedia = new ImageSpan(makeDrawable(uri, getMediaType(uri)), uri.toString());
        span.setSpan(multimedia, 0, strSpanInnerText.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        getEditableText().insert(getSelectionStart() == -1 ? getText().length() : getSelectionStart(), span);
    }

    private static class SerializeObject implements Serializable {

        private byte[] mBackground;
        private String mHtml;
        private ArrayList<AbsoluteSpanDataSet> mAbsoluteSizeSpanSelections; //SIZE, START, END

        private static class AbsoluteSpanDataSet implements Serializable {
            private int mTextSizeWithDip;
            private int mStartSpan;
            private int mEndSpan;
        }
    }

    public void save() {

        final EditText edit = new EditText(getContext());
        edit.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        edit.setHint("FILE NAME");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(edit);
        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String fileName = edit.getText().toString();
                if(fileName == null || fileName.isEmpty()) {
                    fileName = GGomdolUtil.getCurrentDate();
                }

                SerializeObject object = new SerializeObject();

                ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
                ((BitmapDrawable)getBackground()).getBitmap().compress( Bitmap.CompressFormat.JPEG, 100, stream) ;
                byte[] byteArray = stream.toByteArray() ;
                try {
                    stream.close();
                } catch (Exception e) {}

                object.mBackground = byteArray;
                object.mHtml = Html.toHtml(getEditableText());

                AbsoluteSizeSpan spans[] = getEditableText().getSpans(0, getEditableText().length(), AbsoluteSizeSpan.class);

                if(spans != null && spans.length > 0) {
                    object.mAbsoluteSizeSpanSelections = new ArrayList<SerializeObject.AbsoluteSpanDataSet>();

                    for (AbsoluteSizeSpan span : spans) {
                        SerializeObject.AbsoluteSpanDataSet dataSet = new SerializeObject.AbsoluteSpanDataSet();
                        dataSet.mTextSizeWithDip = span.getSize();
                        dataSet.mStartSpan = getEditableText().getSpanStart(span);
                        dataSet.mEndSpan = getEditableText().getSpanEnd(span);

                        object.mAbsoluteSizeSpanSelections.add(dataSet);
                    }
                }

                try {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(DATA_PATH, fileName)));
                    oos.writeObject(object);
                    oos.flush();
                    oos.close();
                    Toast.makeText(getContext(), "Saved... [" + fileName + "]", Toast.LENGTH_SHORT).show();
                } catch(Exception ex) {
                    Toast.makeText(getContext(), "Save fail...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("CANCEL", null);
        builder.create().show();
    }

    public void load() {

        final String fileNames[] = makeGGOMDOLFileListNames();
        if(fileNames == null) {
            return;
        }

        AlertDialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setSingleChoiceItems(fileNames, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(DATA_PATH, fileNames[which])));
                    SerializeObject object = (SerializeObject)ois.readObject();
                    setBackground(new BitmapDrawable(getContext().getResources(), BitmapFactory.decodeByteArray(object.mBackground, 0, object.mBackground.length)));

                    GGOMDOLImageGetter getter = new GGOMDOLImageGetter();
                    setText(Html.fromHtml(object.mHtml, getter, null));

                    for(String source : getter.mContentUrlList) {
                        int index = getEditableText().toString().indexOf(BITMAP_UNICODE);
                        if(index != -1) {
                            setText(getEditableText().replace(index, index + BITMAP_UNICODE.length(), source));
                        }
                    }

                    if(object.mAbsoluteSizeSpanSelections != null || object.mAbsoluteSizeSpanSelections.size() > 0) {
                        for(SerializeObject.AbsoluteSpanDataSet dataset : object.mAbsoluteSizeSpanSelections) {
                            getEditableText().setSpan(new AbsoluteSizeSpan(dataset.mTextSizeWithDip, true), dataset.mStartSpan, dataset.mEndSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    ois.close();
                    Toast.makeText(getContext(), "Loaded...", Toast.LENGTH_SHORT).show();
                } catch(Exception ex) {
                    Toast.makeText(getContext(), "Load fail...", Toast.LENGTH_SHORT).show();
                } finally {
                    dialog.dismiss();
                }
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    public void delete() {

        final String fileNames[] = makeGGOMDOLFileListNames();
        if(fileNames == null) {
            return;
        }

        final boolean isCheckItem[] = new boolean[fileNames.length];
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(fileNames, isCheckItem, null);
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                boolean isDeleted = false;
                SparseBooleanArray array = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
                for(int index = 0; index < array.size() ; index++) {
                    if(array.get(index)) {
                        new File(DATA_PATH, fileNames[index]).delete();
                        isDeleted = true;
                    }
                }

                if(isDeleted) {
                    Toast.makeText(getContext(), "Deleted...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("CANCEL", null);
        builder.create().show();
    }

    private String[] makeGGOMDOLFileListNames() {

        File files[] = makeGGOMDOLFileList();

        if(files == null) {
            return null;
        }

        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        final String fileNames[] = new String[files.length];
        for(int index = 0; index < fileNames.length; index++) {
            fileNames[index] = files[index].getName();
        }
        return fileNames;
    }

    private File[] makeGGOMDOLFileList() {

        File dir = new File(DATA_PATH);
        File[] files = dir.listFiles();

        if(files == null || files.length <=0) {
            Toast.makeText(getContext(), "Not exist save files.", Toast.LENGTH_SHORT).show();
            return null;
        }

        return files;
    }

    private class GGOMDOLImageGetter implements Html.ImageGetter {

        private ArrayList<String> mContentUrlList = new ArrayList<String>();

        @Override
        public Drawable getDrawable(String source) {

            mContentUrlList.add(source);
            Uri uri = Uri.parse(source);
            return makeDrawable(uri, getMediaType(uri));
        }
    }

    public void setTextColor(int color) {
        endComposingText();
        mGGomdolText.setTextColor(color);
    }

    public void setBold(boolean isBold) {
        endComposingText();
        mGGomdolText.setBold(isBold);
    }

    public void setItalic(boolean isItalic) {
        endComposingText();
        mGGomdolText.setItalic(isItalic);
    }

    public void setUnderLine(boolean isUnderLine) {
        endComposingText();
        mGGomdolText.setUnderLine(isUnderLine);
    }

    public void setTextsize(int textSize) {
        endComposingText();
        mGGomdolText.setTextSize(textSize);
    }

    private void endComposingText() {

        beginBatchEdit();
        clearComposingText();
        endBatchEdit();
    }

    public int getTextColor() {
        return mGGomdolText.getTextColor();
    }

    public int getTextsize() {
        return mGGomdolText.getTextSize();
    }

    private Drawable makeDrawable(Uri uri, Type type) {

        Drawable drawable;
        int drawableSize = (int) getTextSize() * 4;

        if (type == Type.IMAGE) {
            drawable = Drawable.createFromPath(GGomdolUtil.getPathFromUri(getContext(), uri));
        } else if (type == Type.VIDEO) {
            drawable = Drawable.createFromPath(GGomdolUtil.getThumbnailPathForLocalFile(getContext(), uri));
            drawable.setBounds(0, 0, drawableSize, drawableSize);
            Bitmap bitmap = Bitmap.createBitmap(drawableSize, drawableSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
            Bitmap backBit = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ggomdol_video);
            canvas.drawBitmap(backBit, new Rect(0, 0, backBit.getWidth(), backBit.getHeight()), new Rect(0, 0, drawableSize, drawableSize), null);
            backBit.recycle();
            drawable = new BitmapDrawable(getResources(), bitmap);
        } else if (type == Type.AUDIO) {
            drawable = getContext().getResources().getDrawable(R.drawable.ggomdol_sound);
        } else {
            drawable = getContext().getResources().getDrawable(R.drawable.ggomdol_unknown);
        }

        drawable.setBounds(0, 0, drawableSize, drawableSize);

        return drawable;
    }

    private Type getMediaType(Uri uri) {

        String type = getContext().getContentResolver().getType(uri);

        if (type.toLowerCase().contains("image")) {
            return Type.IMAGE;
        } else if (type.toLowerCase().contains("video")) {
            return Type.VIDEO;
        } else if (type.toLowerCase().contains("audio")) {
            return Type.AUDIO;
        }

        return Type.UNKOWN;
    }

    private class GGomdolTextWatcher implements TextWatcher {

        @Override public void afterTextChanged(Editable editable) {}
        @Override public void beforeTextChanged(CharSequence charsequence, int start, int before, int after) {}

        @Override
        public void onTextChanged(CharSequence charsequence, int start, int before, int count) {

            if (before == 0 && count > 0 && charsequence.toString().substring(start, start + count).replace(" ", "").length() > 0) {
                mGGomdolText.onTextChangedSpanProcess((Editable) charsequence, start, count);
            }
        }
    }
}