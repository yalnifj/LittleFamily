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
        TreeLoaderTask task = new TreeLoaderTask(this, this);
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
        int minx = 0;
        int miny = 0;
        int maxx = 0;
        int maxy = 0;

        treeBackground = new AnimatedBitmapSprite(BitmapFactory.decodeResource(getResources(), R.drawable.tree_background));
        treeBackground.setWidth(treeView.getWidth());
        treeBackground.setHeight(treeView.getHeight());
        treeView.setBackgroundSprite(treeBackground);

        leftLeaf = BitmapFactory.decodeResource(getResources(), R.drawable.leaf_left);
        rightLeaf = BitmapFactory.decodeResource(getResources(), R.drawable.leaf_right);

        addTreeSprite(root, 0, 0);

        treeView.setMaxWidth(width);
        treeView.setMaxHeight(height);

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
        TreePersonAnimatedSprite sprite = new TreePersonAnimatedSprite(node.getPerson(), this, rightLeaf);
        int xdiff = (int) (sprite.getWidth()*0.75);
        int ydiff = sprite.getHeight()-10;
        if (node.getLeft()!=null) {
            TreePersonAnimatedSprite father = addTreeSprite(node.getLeft(), x-xdiff, y - ydiff);
            x = (int) (father.getX()+xdiff);
            y = (int) (father.getY()+ydiff);
        }
        if (node.getRight()!=null) {
            TreePersonAnimatedSprite mother = addTreeSprite(node.getRight(), x+xdiff, y - ydiff);
            y = (int) (mother.getY()+ydiff);
        }
        sprite.setX(x);
        sprite.setY(y);
        treeView.addSprite(sprite);

        return sprite;
    }

    @Override
    public void onComplete(TreeNode root) {
        this.root = root;

        setupTreeViewSprites();
    }
}
