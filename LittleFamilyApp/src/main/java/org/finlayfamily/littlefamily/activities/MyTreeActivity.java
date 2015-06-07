package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.TreeLoaderTask;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.TreeNode;
import org.finlayfamily.littlefamily.games.DressUpDolls;
import org.finlayfamily.littlefamily.sprites.AnimatedBitmapSprite;
import org.finlayfamily.littlefamily.sprites.Sprite;
import org.finlayfamily.littlefamily.sprites.TreePersonAnimatedSprite;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.finlayfamily.littlefamily.views.TreeSpriteSurfaceView;

import java.util.Collections;
import java.util.List;

public class MyTreeActivity extends LittleFamilyActivity implements TreeLoaderTask.Listener{

    private LittlePerson selectedPerson;
    private DataService dataService;

    private TreeSpriteSurfaceView treeView;
    private AnimatedBitmapSprite treeBackground = null;
    private TreeNode root;
    private Bitmap leftLeaf;
    private Bitmap rightLeaf;
    private Bitmap vineBm;
    private Bitmap vineBm2;
    private Bitmap vineBm3;
    private Bitmap vineH1;
    private Bitmap vineH2;
    private Bitmap matchBtn;
    private Bitmap puzzleBtn;
    private DressUpDolls dressUpDolls;
    private int maxX = 0;
    private int maxY = 0;

    public Bitmap getMatchBtn() {
        return matchBtn;
    }

    public void setMatchBtn(Bitmap matchBtn) {
        this.matchBtn = matchBtn;
    }

    public Bitmap getPuzzleBtn() {
        return puzzleBtn;
    }

    public void setPuzzleBtn(Bitmap puzzleBtn) {
        this.puzzleBtn = puzzleBtn;
    }

    public DressUpDolls getDressUpDolls() {
        return dressUpDolls;
    }

    public void setDressUpDolls(DressUpDolls dressUpDolls) {
        this.dressUpDolls = dressUpDolls;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tree);

        treeView = (TreeSpriteSurfaceView) findViewById(R.id.treeView);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        dataService = DataService.getInstance();
        dataService.setContext(this);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (treeView.getSprites().size()==0) {
            showLoadingDialog();
            TreeLoaderTask task = new TreeLoaderTask(this, this, 0, 2);
            task.execute(selectedPerson);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        treeView.stop();
        treeView.onDestroy();

        if (treeBackground!=null) {
            treeBackground.onDestroy();
        }
        treeBackground = null;
    }

    private void setupTreeViewSprites() {
        dressUpDolls = new DressUpDolls();
        int width = getScreenWidth();
        int height = getScreenHeight();
        maxX = width;
        maxY = height;

        treeBackground = new AnimatedBitmapSprite(BitmapFactory.decodeResource(getResources(), R.drawable.tree_background));
        treeBackground.setWidth(treeView.getWidth());
        treeBackground.setHeight(treeView.getHeight());
        treeView.setBackgroundSprite(treeBackground);

        leftLeaf = BitmapFactory.decodeResource(getResources(), R.drawable.leaf_left);
        rightLeaf = BitmapFactory.decodeResource(getResources(), R.drawable.leaf_right);
        vineBm = BitmapFactory.decodeResource(getResources(), R.drawable.vine);
        vineBm2 = BitmapFactory.decodeResource(getResources(), R.drawable.vine2);
        vineBm3 = BitmapFactory.decodeResource(getResources(), R.drawable.vine3);
        vineH1 = BitmapFactory.decodeResource(getResources(), R.drawable.vineh);
        vineH2 = BitmapFactory.decodeResource(getResources(), R.drawable.vineh2);

        BitmapFactory.Options opts = new BitmapFactory.Options();

        matchBtn = ImageHelper.loadBitmapFromResource(this, R.drawable.house_familyroom_frame, 0, 80, 80);
        puzzleBtn = ImageHelper.loadBitmapFromResource(this, R.drawable.house_toys_blocks, 0, 80, 80);

        TreePersonAnimatedSprite rootSprite = addTreeSprite(root, 20, 20, true);

        treeView.setMaxWidth(maxX);
        treeView.setMaxHeight(maxY);
        int clipX = (int) rootSprite.getX() - width/2 - rootSprite.getWidth()/2;
        int clipY = (int) rootSprite.getY() - height/2;
        if (clipX < 0) clipX = 0;
        if (clipY < 0) clipY = 0;
        treeView.setClipX(clipX);
        treeView.setClipY(clipY);

        //-- prevent overlapping
        List<Sprite> sprites = treeView.getSprites();
        Collections.reverse(sprites);

        hideLoadingDialog();
    }

    /**
     * recursively walk the tree and add the sprites
     * automatically adjusts positions based on position of parent nodes
     * @param node
     * @param x
     * @param y
     * @return
     */
    public TreePersonAnimatedSprite addTreeSprite(TreeNode node, int x, int y, boolean leftSide) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        int treeWidth = 0;

        //-- basis case
        if (node.getLeft()==null && node.getRight()==null) {
            TreePersonAnimatedSprite sprite = new TreePersonAnimatedSprite(node, this, leftLeaf, rightLeaf);
            sprite.setX(x);
            sprite.setY(y);
            if (x+sprite.getWidth() > maxX) maxX = x+sprite.getWidth();
            if (y+sprite.getHeight() > maxY) maxY = y+sprite.getHeight();
            sprite.setTreeWidth(sprite.getWidth());
            if(node.getDepth()>0) {
                addDownVine(sprite, leftSide);
            }
            treeView.addSprite(sprite);
            return sprite;
        }

        //-- fathers side
        int cy = y;
        if (node.getLeft()!=null) {
            TreePersonAnimatedSprite father = addTreeSprite(node.getLeft(), x, y, true);
            cy = (int) (father.getY() + father.getHeight()+vineBm.getHeight());
            if (node.getRight()!=null) {
                x = x + father.getTreeWidth()+20;
            } else {
                x = x + father.getTreeWidth()/2;
            }

            AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineH2);
            vine.setX(father.getX() - 40 + father.getWidth() / 2);
            vine.setY(cy - 71);
            treeView.addSprite(vine);
            boolean flip = true;
            int vx = (int) (vine.getX() + vine.getWidth()-10);
            while(vx < x) {
                Bitmap bv = vineH2;
                int vy = cy - 71;
                if (flip) {
                    bv = vineH1;
                    vy = cy-90;
                }
                vine = new AnimatedBitmapSprite(bv);
                vine.setX(vx);
                vine.setY(vy);
                treeView.addSprite(vine);
                vx = (int) (vine.getX() + vine.getWidth()-10);
                flip = !flip;
            }

            treeWidth = father.getTreeWidth();
            if (x+father.getWidth() > maxX) maxX = x+father.getTreeWidth();
            if (y+father.getHeight() > maxY) maxY = y+father.getHeight();
        }

        //-- mothers side
        if (node.getRight()!=null) {
            TreePersonAnimatedSprite mother = addTreeSprite(node.getRight(), x, y, false);
            cy = (int) (mother.getY() + mother.getHeight()+vineBm.getHeight());
            if (x+mother.getWidth() > maxX) maxX = x+mother.getWidth();
            if (y+mother.getHeight() > maxY) maxY = y+mother.getHeight();
            treeWidth = treeWidth + mother.getTreeWidth();

            AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineH1);
            vine.setX(mother.getX()-45);
            vine.setY(cy-90);
            treeView.addSprite(vine);
            boolean flip = true;
            int vx = (int) (vine.getX() - vine.getWidth() - 5);
            while(vx > x + leftLeaf.getWidth()+10) {
                Bitmap bv = vineH1;
                int vy = cy-90;
                if (flip) {
                    bv = vineH2;
                    vy = cy - 71;
                }
                vine = new AnimatedBitmapSprite(bv);
                vine.setX(vx);
                vine.setY(vy);
                treeView.addSprite(vine);
                vx = (int) (vine.getX() - vine.getWidth() - 5);
                flip = !flip;
            }
        }

        //--child side
        TreePersonAnimatedSprite sprite = new TreePersonAnimatedSprite(node, this, leftLeaf, rightLeaf);
        sprite.setX(x - (sprite.getWidth() / 2) - (node.getLeft() != null ? 10 : 0));
        sprite.setY(cy);
        if (x+sprite.getWidth() > maxX) maxX = x+sprite.getWidth();
        if (cy+sprite.getHeight() > maxY) maxY = cy+sprite.getHeight();

        AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineBm2);
        vine.setX(sprite.getX()+leftLeaf.getWidth()-40);
        vine.setY(sprite.getY() - 40);
        treeView.addSprite(vine);
        if (node.getDepth()>0) {
            addDownVine(sprite, leftSide);
        }

        treeView.addSprite(sprite);

        sprite.setTreeWidth(treeWidth);
        return sprite;
    }

    private void addDownVine(TreePersonAnimatedSprite sprite, boolean leftSide) {
        Bitmap vbm = vineBm;
        if (!leftSide) vbm = vineBm3;
        AnimatedBitmapSprite vine2 = new AnimatedBitmapSprite(vbm);
        if (!leftSide) {
            vine2.setX(sprite.getX()+leftLeaf.getWidth() - 55);
            vine2.setY(sprite.getY() + sprite.getHeight()-55);
        } else {
            vine2.setX(sprite.getX()+leftLeaf.getWidth()-65);
            vine2.setY(sprite.getY() + sprite.getHeight()-53);
        }
        treeView.addSprite(vine2);
    }

    @Override
    public void onComplete(TreeNode root) {
        this.root = root;

        setupTreeViewSprites();
    }
}
