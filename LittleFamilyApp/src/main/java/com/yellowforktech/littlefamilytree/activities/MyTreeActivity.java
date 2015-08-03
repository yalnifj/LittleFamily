package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.TreeLoaderTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.TreeNode;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.games.DressUpDolls;
import com.yellowforktech.littlefamilytree.games.TreeSearchGame;
import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.sprites.TouchEventGameSprite;
import com.yellowforktech.littlefamilytree.sprites.TreePersonAnimatedSprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.views.TreeSpriteSurfaceView;

import org.gedcomx.types.GenderType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTreeActivity extends LittleFamilyActivity implements TreeLoaderTask.Listener, EventListener {
    public static final String TOPIC_NAVIGATE_UP_TREE = "navigateUpTree";
    public static final String TOPIC_START_FIND_PERSON = "startFindPerson";
    public static final String TOPIC_PERSON_SELECTED = "personSelected";
    public static final String DATA_TREE_NODE = "dataTreeNode";
    public static final int buttonSize = 100;
    private LittlePerson selectedPerson;
    private DataService dataService;

    private TreeSpriteSurfaceView treeView;
    private AnimatedBitmapSprite treeBackground = null;
    private TreeNode root;
    private TreePersonAnimatedSprite rootSprite;
    private Bitmap leftLeaf;
    private Bitmap rightLeaf;
    private Bitmap vineBm;
    private Bitmap vineBm2;
    private Bitmap vineBm3;
    private Bitmap vineH1;
    private Bitmap vineH2;
    private Bitmap vineArrow;
    private Bitmap matchBtn;
    private Bitmap puzzleBtn;
    private DressUpDolls dressUpDolls;
    private Bitmap pencilBtn;
    private Bitmap paintBtn;
    private Bitmap bubbleBtn;
    private int maxX = 0;
    private int maxY = 0;
    private TouchEventGameSprite touchedArrow;
    private Map<Integer, List<Sprite>> loadedLevels;
    private Map<Integer, List<Sprite>> levelArrows;
    private int maxLevel=2;

    private TreeSearchGame treeSearchGame;

    public Bitmap getMatchBtn() {
        return matchBtn;
    }

    public Bitmap getPuzzleBtn() {
        return puzzleBtn;
    }

    public DressUpDolls getDressUpDolls() {
        return dressUpDolls;
    }

    public Bitmap getPaintBtn() {
        return paintBtn;
    }

    public Bitmap getPencilBtn() {
        return pencilBtn;
    }

    public Bitmap getBubbleBtn() {
        return bubbleBtn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.yellowforktech.littlefamilytree.R.layout.activity_my_tree);

        treeView = (TreeSpriteSurfaceView) findViewById(com.yellowforktech.littlefamilytree.R.id.treeView);
        Bitmap starBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star1);
        treeView.setStarBitmap(starBitmap);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        dataService = DataService.getInstance();
        dataService.setContext(this);

        EventQueue.getInstance().subscribe(TOPIC_NAVIGATE_UP_TREE, this);
        EventQueue.getInstance().subscribe(TOPIC_START_FIND_PERSON, this);
        EventQueue.getInstance().subscribe(TOPIC_PERSON_SELECTED, this);
        loadedLevels = new HashMap<>();
        levelArrows = new HashMap<>();

        treeSearchGame = new TreeSearchGame(this);

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
    protected void onStop() {
        super.onStop();
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
        EventQueue.getInstance().unSubscribe(TOPIC_NAVIGATE_UP_TREE, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_FIND_PERSON, this);
        EventQueue.getInstance().unSubscribe(TOPIC_PERSON_SELECTED, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        treeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        treeView.resume();
    }

    private void setupTreeViewSprites() {
        dressUpDolls = new DressUpDolls();
        int width = getScreenWidth();
        int height = getScreenHeight();
        maxX = width;
        maxY = height;

        treeBackground = new AnimatedBitmapSprite(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.tree_background));
        treeBackground.setWidth(treeView.getWidth());
        treeBackground.setHeight(treeView.getHeight());
        treeView.setBackgroundSprite(treeBackground);

        leftLeaf = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.leaf_left);
        rightLeaf = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.leaf_right);
        vineBm = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.vine);
        vineBm2 = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.vine2);
        vineBm3 = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.vine3);
        vineH1 = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.vineh);
        vineH2 = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.vineh2);
        vineArrow = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.vine_arrow);

        BitmapFactory.Options opts = new BitmapFactory.Options();

        matchBtn = ImageHelper.loadBitmapFromResource(this, com.yellowforktech.littlefamilytree.R.drawable.house_familyroom_frame, 0, buttonSize, buttonSize);
        puzzleBtn = ImageHelper.loadBitmapFromResource(this, com.yellowforktech.littlefamilytree.R.drawable.house_toys_blocks, 0, buttonSize, buttonSize);
        paintBtn = ImageHelper.loadBitmapFromResource(this, com.yellowforktech.littlefamilytree.R.drawable.painting, 0, buttonSize, buttonSize);
        pencilBtn = ImageHelper.loadBitmapFromResource(this, com.yellowforktech.littlefamilytree.R.drawable.pencils, 0, buttonSize, buttonSize);
        bubbleBtn = ImageHelper.loadBitmapFromResource(this, com.yellowforktech.littlefamilytree.R.drawable.bubble, 0, buttonSize, buttonSize);

        rootSprite = addTreeSprite(root, 20, 40, true);
        if (root.getChildren()!=null && root.getChildren().size()>0) {
            AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineBm2);
            vine.setX(rootSprite.getX()+leftLeaf.getWidth()-43);
            vine.setY(rootSprite.getY() - 63);
            treeView.addSprite(vine);
            addDownVine(rootSprite, false);
            addChildSprites(root.getChildren(), rootSprite.getX(), rootSprite.getY()+rootSprite.getHeight()+vineBm.getHeight());
        } else if (root.getLeft()!=null && root.getLeft().getChildren()!=null && root.getLeft().getChildren().size()>0){
            treeView.getSprites().remove(rootSprite);
            addChildSprites(root.getLeft().getChildren(), rootSprite.getX(), rootSprite.getY());
        }

        treeView.setMaxWidth(maxX * 2);
        treeView.setMaxHeight(maxY * 2);
        int clipX = (int) rootSprite.getX() - width/2 - rootSprite.getWidth()/2;
        if (clipX < 0) clipX = 0;
        int clipY = 0;
        treeView.setClipX(clipX);
        treeView.setClipY(clipY);

        //-- prevent overlapping
        //List<Sprite> sprites = treeView.getSprites();
        //Collections.reverse(sprites);

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
        if (loadedLevels.get(node.getDepth())==null) loadedLevels.put(node.getDepth(), new ArrayList<Sprite>());
        if (levelArrows.get(node.getDepth())==null) levelArrows.put(node.getDepth(), new ArrayList<Sprite>());
        if (loadedLevels.get(node.getDepth()+1)==null) loadedLevels.put(node.getDepth()+1, new ArrayList<Sprite>());

        //-- basis case
        if (node.getLeft()==null && node.getRight()==null) {
            TreePersonAnimatedSprite sprite = new TreePersonAnimatedSprite(node, this, leftLeaf, rightLeaf, true);
            sprite.setX(x);
            sprite.setY(y);

            if (node.isHasParents()) {
                TouchEventGameSprite upArrow = new TouchEventGameSprite(this.vineArrow, TOPIC_NAVIGATE_UP_TREE);
                upArrow.setX(sprite.getX() + sprite.getWidth() / 2 - upArrow.getWidth() / 2);
                upArrow.setY(y);
                upArrow.setData(DATA_TREE_NODE, node);
                upArrow.setIgnoreAlpha(true);
                y = y + upArrow.getHeight();
                sprite.setY(y);
                treeView.addSprite(upArrow);
                levelArrows.get(node.getDepth()).add(upArrow);
                loadedLevels.get(node.getDepth()).add(upArrow);
            }

            if (x+sprite.getWidth() > maxX) maxX = x+sprite.getWidth();
            if (y+sprite.getHeight() > maxY) maxY = y+sprite.getHeight();
            sprite.setTreeWidth(sprite.getWidth());
            if(node.getDepth()>0) {
                addDownVine(sprite, leftSide);
            }
            treeView.addSprite(sprite);
            loadedLevels.get(node.getDepth()).add(sprite);

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

            if (node.getLeft().getChildren()==null) {
                AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineH2);
                vine.setX(father.getX() - 11 + father.getWidth() / 2);
                vine.setY(cy - 64);
                treeView.addSprite(vine);
                loadedLevels.get(father.getNode().getDepth()).add(vine);
                boolean flip = true;
                int vx = (int) (vine.getX() + vine.getWidth() - 4);
                while (vx + 45 < x) {
                    Bitmap bv = vineH2;
                    int vy = cy - 64;
                    if (flip) {
                        bv = vineH1;
                        vy = cy - 121;
                    }
                    vine = new AnimatedBitmapSprite(bv);
                    vine.setX(vx);
                    vine.setY(vy);
                    treeView.addSprite(vine);
                    loadedLevels.get(father.getNode().getDepth()).add(vine);
                    vx = (int) (vine.getX() + vine.getWidth() - 4);
                    flip = !flip;
                }
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

            if (node.getRight().getChildren()==null) {
                AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineH1);
                vine.setX(mother.getX() - 46);
                vine.setY(cy - 121);
                treeView.addSprite(vine);
                loadedLevels.get(mother.getNode().getDepth()).add(vine);
                boolean flip = true;
                int vx = (int) (vine.getX() - vine.getWidth() - 8);
                while (vx > x -45) {
                    Bitmap bv = vineH1;
                    int vy = cy - 80;
                    if (flip) {
                        bv = vineH2;
                        vy = cy - 64;
                    }
                    vine = new AnimatedBitmapSprite(bv);
                    vine.setX(vx);
                    vine.setY(vy);
                    if (flip) {
                        vine.setWidth(vineH2.getWidth()+20);
                    }
                    treeView.addSprite(vine);
                    loadedLevels.get(mother.getNode().getDepth()).add(vine);
                    vx = (int) (vine.getX() - vine.getWidth() - 4);
                    flip = !flip;
                }
            }
        }

        //--child side
        TreePersonAnimatedSprite sprite = new TreePersonAnimatedSprite(node, this, leftLeaf, rightLeaf, node.getDepth()==0);
        sprite.setX(x - (sprite.getWidth() / 2) - (node.getLeft() != null ? 10 : 0));
        sprite.setY(cy);
        if (x+sprite.getWidth() > maxX) maxX = x+sprite.getWidth();
        if (cy+sprite.getHeight() > maxY) maxY = cy+sprite.getHeight();

        if (node.getDepth()>0) {
            AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineBm2);
            vine.setX(sprite.getX()+leftLeaf.getWidth()-43);
            vine.setY(sprite.getY() - 63);
            treeView.addSprite(vine);
            loadedLevels.get(node.getDepth()).add(vine);
            addDownVine(sprite, leftSide);
        }

        treeView.addSprite(sprite);
        loadedLevels.get(node.getDepth()).add(sprite);

        sprite.setTreeWidth(treeWidth);
        return sprite;
    }

    private void addChildSprites(List<LittlePerson> children, float x, float y) {
        if (children.size()>1) {
            x = x - leftLeaf.getWidth() * children.size()/2;
            if (x<50) x = 50;
        }
        y = y - 15;
        float vx = x+10;
        List<TreePersonAnimatedSprite> childSprites = new ArrayList<>();
        for(LittlePerson child : children) {
            TreePersonAnimatedSprite childSprite = null;
            if (!child.equals(root.getPerson())) {
                TreeNode childNode = new TreeNode();
                childNode.setPerson(child);
                childNode.setDepth(0);
                childNode.setIsRoot(false);
                childSprite = addTreeSprite(childNode, (int)x, (int)y-60, true);
                treeView.removeSprite(childSprite);
                childSprites.add(childSprite);
                x = x+childSprite.getWidth()+50;
            } else {
                childSprite = rootSprite;
                rootSprite.setX(x);
                rootSprite.setY(y-60);
                childSprites.add(rootSprite);
                x = x+rootSprite.getWidth()+50;
            }
/*
            if (child.getGender()== GenderType.Female) {
                AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineBm);
                vine.setX(childSprite.getX() - 55);
                vine.setY(childSprite.getY() - 38);
                vine.setHeight(vineBm.getHeight()+10);
                treeView.addSprite(vine);
                loadedLevels.get(childSprite.getNode().getDepth()).add(vine);
            } else {
                AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineBm2);
                vine.setX(childSprite.getX() + leftLeaf.getWidth() - 53);
                vine.setY(childSprite.getY() - 36);
                vine.setHeight(vineBm2.getHeight()-25);
                treeView.addSprite(vine);
                loadedLevels.get(childSprite.getNode().getDepth()).add(vine);
            }
            */
        }

        boolean flip = true;
        while(vx < x - leftLeaf.getWidth()) {
            Bitmap bv = vineH1;
            float vy = y-104;
            if (flip) {
                bv = vineH2;
                vy = y - 48;
            }
            AnimatedBitmapSprite vine = new AnimatedBitmapSprite(bv);
            vine.setX(vx);
            vine.setY(vy);
            treeView.addSprite(vine);
            vx = vine.getX() + vine.getWidth() - 5;
            flip = !flip;
        }

        for(TreePersonAnimatedSprite childSprite : childSprites) {
            treeView.addSprite(childSprite);
        }
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
        loadedLevels.get(sprite.getNode().getDepth()).add(vine2);
    }

    @Override
    public void onComplete(TreeNode root) {
        if (root.getDepth()==0) {
            this.root = root;
            setupTreeViewSprites();
        } else {
            TreeNode node = (TreeNode) touchedArrow.getData(DATA_TREE_NODE);
            Integer level = node.getDepth()+1;
            for(int l = level; l<=maxLevel; l++) {
                if (loadedLevels.get(l)!=null && loadedLevels.get(l).size()>0) {
                    for(Sprite s : loadedLevels.get(l)) {
                        treeView.removeSprite(s);
                    }
                    loadedLevels.get(l).clear();
                }
            }
            if (levelArrows.get(level-1)!=null) {
                for(Sprite a : levelArrows.get(level-1)) {
                    if (!treeView.getSprites().contains(a)) {
                        treeView.addSprite(a);
                    }
                }
            }
            maxLevel = level;
            loadedLevels.put(level, new ArrayList<Sprite>());

            List<Sprite> oldSprites = new ArrayList<>(treeView.getSprites());
            int xdiff = 0;
            int ydiff = 0;

            int x = (int) touchedArrow.getX()+30 - leftLeaf.getWidth()*2;
            if (x < 0) {
                xdiff = 0 - x;
                x = 0;
            }
            int y = (int) touchedArrow.getY()+20;

            AnimatedBitmapSprite vineh = new AnimatedBitmapSprite(vineBm2);
            vineh.setX(touchedArrow.getX() - 12 + xdiff);
            vineh.setY(y);
            treeView.addSprite(vineh);
            loadedLevels.get(level).add(vineh);

            if (root.getLeft()!=null) {
                node.setLeft(root.getLeft());
                TreePersonAnimatedSprite sprite = addTreeSprite(root.getLeft(), x, y, true);
                int cy = (int) (sprite.getY() + sprite.getHeight()+vineBm.getHeight());
                AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineH2);
                vine.setX(sprite.getX() - 11 + sprite.getWidth() / 2);
                vine.setY(cy - 64);
                treeView.addSprite(vine);
                loadedLevels.get(level).add(vine);
                xdiff = (int) (xdiff + sprite.getX()-x);
                ydiff = (int) (sprite.getY()+sprite.getHeight()+vine.getHeight()+71 - y);
                x = x + sprite.getTreeWidth();

            } else {
                x = (int) touchedArrow.getX() + leftLeaf.getWidth();
            }
            if (root.getRight()!=null) {
                node.setRight(root.getRight());
                TreePersonAnimatedSprite sprite = addTreeSprite(root.getRight(), x, y, false);
                AnimatedBitmapSprite vine = new AnimatedBitmapSprite(vineH1);
                vine.setX(sprite.getX() - 46);
                int cy = (int) (sprite.getY() + sprite.getHeight()+vineBm.getHeight());
                vine.setY(cy - 114);
                treeView.addSprite(vine);
                loadedLevels.get(level).add(vine);
                int ydiff2 = (int) (sprite.getY()+sprite.getHeight()+vine.getHeight()+90 - y);
                if (ydiff2>ydiff) ydiff = ydiff2;
            }
            if (xdiff>0 || ydiff >0) {
                treeView.setMaxHeight(treeView.getMaxHeight() + ydiff);
                treeView.setMaxWidth(treeView.getMaxWidth() + xdiff);
                vineh.setY(vineh.getY() + ydiff);
                for(Sprite s : oldSprites) {
                    s.setX(s.getX()+xdiff);
                    s.setY(s.getY()+ydiff);
                }
            }
            treeView.removeSprite(touchedArrow);
            hideLoadingDialog();
        }
    }

    @Override
    public void onEvent(String topic, Object o) {
        super.onEvent(topic, o);

        if (topic.equals(TOPIC_NAVIGATE_UP_TREE)) {
            touchedArrow = (TouchEventGameSprite) o;
            TreeNode node = (TreeNode) touchedArrow.getData(DATA_TREE_NODE);
            LittlePerson person = node.getPerson();
            if (person.getGender()==GenderType.Female && node.getSpouse()!=null) {
                person = node.getSpouse();
            }
            showLoadingDialog();
            TreeLoaderTask task = new TreeLoaderTask(this, this, node.getDepth(), 0);
            task.execute(person);

        } else if (topic.equals(TOPIC_START_FIND_PERSON)) {
            if (treeSearchGame.isComplete()) {
                treeSearchGame.findRandomPerson(root);
            } else {
                treeSearchGame.nextClue();
            }

            speak(treeSearchGame.getClueText());
        } else if (topic.equals(TOPIC_PERSON_SELECTED)) {
            if (isTreeSearchGameActive()) {
                Map<String, Object> params = (Map<String, Object>) o;
                if (params!=null) {
                    TreeNode node = (TreeNode) params.get("node");
                    boolean isSpouse = (Boolean) params.get("isSpouse");
                    if (treeSearchGame.isMatch(node, isSpouse)) {
                        playCompleteSound();
                        Sprite sprite = (Sprite) params.get("sprite");
                        Rect rect = new Rect();
                        if ((isSpouse && node.getSpouse()!=null && node.getSpouse().getGender()==GenderType.Female)
                                || (!isSpouse && node.getPerson().getGender()==GenderType.Female)) {
                            rect.set((int)(sprite.getX() + sprite.getWidth()/2), (int) sprite.getY(), (int) (sprite.getX() + sprite.getWidth()), (int) (sprite.getY() + sprite.getHeight()));
                        } else {
                            rect.set((int) sprite.getX(), (int) sprite.getY(), (int) (sprite.getX() + sprite.getWidth()/2), (int) (sprite.getY() + sprite.getHeight()));
                        }
                        treeView.addStars(rect, false, 10);
                    } else {
                        playBuzzSound();
                    }
                }
            }
        }
    }

    public boolean isTreeSearchGameActive() {
        if (treeSearchGame.getTargetNode()==null) return false;
        return !treeSearchGame.isComplete();
    }
}