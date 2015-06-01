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
import org.finlayfamily.littlefamily.sprites.Sprite;
import org.finlayfamily.littlefamily.sprites.TreePersonAnimatedSprite;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.finlayfamily.littlefamily.views.SpritedClippedSurfaceView;

import java.util.ArrayList;
import java.util.HashMap;

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

        if (treeView.getSprites()!=null) {
            for(Sprite s : treeView.getSprites()) {
                s.onDestroy();
            }
            treeView.getSprites().clear();
        }
        if (treeBackground!=null) {
            treeBackground.onDestroy();
        }
        treeBackground = null;
    }

    private void setupTreeViewSprites() {
        int width = 600;
        int height = 600;
        if (root.getChildren()!=null) {
            int childW = root.getChildren().size()*200;
            if (childW > width) width = childW;
        }
        treeView.setMaxWidth(width);
        treeView.setMaxHeight(height);

        treeBackground = new AnimatedBitmapSprite(BitmapFactory.decodeResource(getResources(), R.drawable.tree_background));
        treeBackground.setWidth(treeView.getWidth());
        treeBackground.setHeight(treeView.getHeight());
        treeView.setBackgroundSprite(treeBackground);

        leftLeaf = BitmapFactory.decodeResource(getResources(), R.drawable.leaf_left);
        rightLeaf = BitmapFactory.decodeResource(getResources(), R.drawable.leaf_right);

        TreePersonAnimatedSprite rootSprite = new TreePersonAnimatedSprite(root.getPerson(), this, rightLeaf);
        rootSprite.setX(width / 2 - rootSprite.getWidth() / 2);
        rootSprite.setY(height / 2 - rootSprite.getHeight() / 2);
        treeView.addSprite(rootSprite);

        addNode(root.getLeft(), (int) rootSprite.getX() - rootSprite.getWidth(), (int) (rootSprite.getY() - rootSprite.getHeight() - 10));
        addNode(root.getRight(), (int) rootSprite.getX() + rootSprite.getWidth(), (int) (rootSprite.getY() - rootSprite.getHeight() - 10));

        hideLoadingDialog();
    }

    private void addNode(TreeNode node, int x, int y) {
        if (node==null) return;
        TreePersonAnimatedSprite sprite = new TreePersonAnimatedSprite(node.getPerson(), this, leftLeaf);
        sprite.setX(x);
        sprite.setY(y);
        treeView.addSprite(sprite);
    }

    @Override
    public void onComplete(TreeNode root) {
        this.root = root;

        setupTreeViewSprites();
    }
}
