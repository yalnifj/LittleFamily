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
import org.finlayfamily.littlefamily.sprites.AnimatedBitmapSprite;
import org.finlayfamily.littlefamily.sprites.TreePersonAnimatedSprite;
import org.finlayfamily.littlefamily.views.SpritedClippedSurfaceView;

public class MyTreeActivity extends LittleFamilyActivity implements TreeLoaderTask.Listener{

    private LittlePerson selectedPerson;
    private DataService dataService;

    private SpritedClippedSurfaceView treeView;
    private AnimatedBitmapSprite treeBackground = null;
    private TreeNode root;
    private Bitmap leftLeaf;
    private Bitmap rightLeaf;
    private int maxX = 0;
    private int maxY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tree);

        treeView = (SpritedClippedSurfaceView) findViewById(R.id.treeView);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        dataService = DataService.getInstance();
        dataService.setContext(this);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();

        showLoadingDialog();
        TreeLoaderTask task = new TreeLoaderTask(this, this, 0, 2);
        task.execute(selectedPerson);
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

        TreePersonAnimatedSprite rootSprite = addTreeSprite(root, 0, 0);

        treeView.setMaxWidth(maxX);
        treeView.setMaxHeight(maxY);
        int clipX = (int) rootSprite.getX() - width/2 - rootSprite.getWidth()/2;
        int clipY = (int) rootSprite.getY() - height/2;
        if (clipX < 0) clipX = 0;
        if (clipY < 0) clipY = 0;
        treeView.setClipX(clipX);
        treeView.setClipY(clipY);

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
    public TreePersonAnimatedSprite addTreeSprite(TreeNode node, int x, int y) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        int treeWidth = 0;

        //-- basis case
        if (node.getLeft()==null && node.getRight()==null) {
            TreePersonAnimatedSprite sprite = new TreePersonAnimatedSprite(node, this, leftLeaf, rightLeaf);
            sprite.setX(x);
            sprite.setY(y);
            treeView.addSprite(sprite);
            if (x+sprite.getWidth() > maxX) maxX = x+sprite.getWidth();
            if (y+sprite.getHeight() > maxY) maxY = y+sprite.getHeight();
            sprite.setTreeWidth(sprite.getWidth());
            return sprite;
        }

        int cy = y;
        if (node.getLeft()!=null) {
            TreePersonAnimatedSprite father = addTreeSprite(node.getLeft(), x, y);
            cy = (int) (father.getY() + father.getHeight()+25);
            if (node.getRight()!=null) {
                x = x + father.getTreeWidth()+20;
            } else {
                x = x + father.getTreeWidth()/2;
            }
            treeWidth = father.getTreeWidth();
            if (x+father.getWidth() > maxX) maxX = x+father.getTreeWidth();
            if (y+father.getHeight() > maxY) maxY = y+father.getHeight();
        }

        if (node.getRight()!=null) {
            TreePersonAnimatedSprite mother = addTreeSprite(node.getRight(), x, y);
            cy = (int) (mother.getY() + mother.getHeight()+25);
            if (x+mother.getWidth() > maxX) maxX = x+mother.getWidth();
            if (y+mother.getHeight() > maxY) maxY = y+mother.getHeight();
            treeWidth = treeWidth + mother.getTreeWidth();
        }

        TreePersonAnimatedSprite sprite = new TreePersonAnimatedSprite(node, this, leftLeaf, rightLeaf);
        sprite.setX(x - (sprite.getWidth() / 2) - (node.getLeft()!=null?10:0));
        sprite.setY(cy);
        treeView.addSprite(sprite);
        if (x+sprite.getWidth() > maxX) maxX = x+sprite.getWidth();
        if (cy+sprite.getHeight() > maxY) maxY = cy+sprite.getHeight();

        sprite.setTreeWidth(treeWidth);
        return sprite;
    }

    @Override
    public void onComplete(TreeNode root) {
        this.root = root;

        setupTreeViewSprites();
    }
}
