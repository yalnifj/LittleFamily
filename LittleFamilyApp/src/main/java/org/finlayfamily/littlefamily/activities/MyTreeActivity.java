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
import org.gedcomx.types.GenderType;

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
        TreeLoaderTask task = new TreeLoaderTask(this, this, 0);
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
        int clipX = (int) rootSprite.getX() - width/2;
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

        int ny = y;
        int mx = x;
        int my = y;
        if (node.getLeft()!=null) {
            TreePersonAnimatedSprite father = addTreeSprite(node.getLeft(), x, y);
            ny = (int) (father.getY()+father.getHeight()+10);
            mx = (int) (father.getX()+father.getWidth());
            my = (int) father.getY();
            x = (int) (father.getX());
        }
        if (node.getRight()!=null) {
            TreePersonAnimatedSprite mother = addTreeSprite(node.getRight(), mx, my);
            ny = (int) (mother.getY()+mother.getHeight()+10);
        }

        Bitmap leaf = leftLeaf;
        if (node.getPerson().getGender()== GenderType.Female) {
            leaf = rightLeaf;
        }
        TreePersonAnimatedSprite sprite = new TreePersonAnimatedSprite(node, this, leaf);
        //if (node.getPerson().getGender()== GenderType.Female) {
        //    x = x+sprite.getWidth();
        //}

        sprite.setX(x);
        sprite.setY(ny);
        treeView.addSprite(sprite);

        if (x+sprite.getWidth() > maxX) maxX = x+sprite.getWidth();
        if (y+sprite.getHeight() > maxY) maxY = y+sprite.getHeight();

        return sprite;
    }

    @Override
    public void onComplete(TreeNode root) {
        this.root = root;

        setupTreeViewSprites();
    }
}
