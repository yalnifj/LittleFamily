package com.yellowforktech.littlefamilytree.games;

import android.util.Log;

import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.activities.tasks.FamilyLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.MemoriesLoaderTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.Media;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Parents on 8/16/2015.
 */
public class RandomMediaChooser implements MemoriesLoaderTask.Listener {
    private List<LittlePerson> people;
	private LinkedList<LittlePerson> familyLoaderQueue;
    private LittlePerson selectedPerson;
    private Media photo;
    private List<Media> usedPhotos;
    private List<LittlePerson> noPhotos;
    private LittleFamilyActivity activity;
    private RandomMediaListener listener;

    private int backgroundLoadIndex = 0;
    private int counter = 0;

    private int maxTries = 20;
    private int maxUsed = 20;
	
	private Random random;
	
	private static RandomMediaChooser instance;

	public void setListener(RandomMediaListener listener)
	{
		this.listener = listener;
	}

	public RandomMediaListener getListener()
	{
		return listener;
	}

	public void setActivity(LittleFamilyActivity activity)
	{
		this.activity = activity;
	}

	public LittleFamilyActivity getActivity()
	{
		return activity;
	}
	
	public static RandomMediaChooser getInstance() {
		if (instance==null) {
			instance = new RandomMediaChooser();
		}
		return instance;
	}

    private RandomMediaChooser() {
        this.people = new LinkedList<>();
		
		familyLoaderQueue = new LinkedList<>();

        usedPhotos = new ArrayList<>(maxUsed);
        noPhotos = new ArrayList<>();
		
		random = new Random();
    }

    public int getMaxTries() {
        return maxTries;
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

    public int getMaxUsed() {
        return maxUsed;
    }

    public void setMaxUsed(int maxUsed) {
        this.maxUsed = maxUsed;
    }

    public LittlePerson getSelectedPerson() {
        return selectedPerson;
    }

    public void setSelectedPerson(LittlePerson selectedPerson) {
        this.selectedPerson = selectedPerson;
    }

    public List<LittlePerson> getPeople() {
        return people;
    }

    public void addPeople(List<LittlePerson> people) {
        for (LittlePerson person : people) {
			 if (!this.people.contains(person)) {
				 if (person.isHasMedia()!=null && person.isHasMedia()==false) {
					 noPhotos.add(person);
				 } else {
					this.people.add(person);
				 }
             }
             if (!familyLoaderQueue.contains(person)) {
                 familyLoaderQueue.add(person);
             }
		}
    }

    public void loadRandomImage() {
        counter++;
        if (people != null && people.size() > 0) {
            Random rand = new Random();
            selectedPerson = people.get(rand.nextInt(people.size()));
            MemoriesLoaderTask task = new MemoriesLoaderTask(this, activity);
            task.execute(selectedPerson);
        }
    }

    public void loadMoreFamilyMembers() {
        if (familyLoaderQueue.size()>0) {
            counter++;
            FamilyLoaderTask task = new FamilyLoaderTask(new FamilyLoaderListener(), activity);
			selectedPerson = familyLoaderQueue.poll();
            if (selectedPerson!=null) {
                task.execute(selectedPerson);
            }
        } else {
            loadRandomImage();
        }
    }

    @Override
    public void onComplete(ArrayList<Media> photos) {
        if (photos == null || photos.size() == 0) {
			people.remove(selectedPerson);
			noPhotos.add(selectedPerson);
            if (backgroundLoadIndex < maxTries && counter < maxTries) {
                loadMoreFamilyMembers();
            } else {
                DataService dataService = DataService.getInstance();
                long mediaCount = 0;
                try {
                    mediaCount = dataService.getDBHelper().getMediaCount();
                } catch (Exception e) {
                    Log.e(getClass().getName(), "Error checking database for random media", e);
                }
                if (mediaCount > 0) {
                    try {
						selectedPerson = dataService.getDBHelper().getRandomPersonWithMedia();
						photos = (ArrayList<Media>) dataService.getDBHelper().getMediaForPerson(selectedPerson.getId());
                        int index = random.nextInt(photos.size());
						int origIndex = index;
						photo = photos.get(index);
						while (usedPhotos.contains(photo)) {
							index++;
							if (index >= photos.size()) index = 0;
							photo = photos.get(index);
							//-- stop if we've used all of these images
							if (index == origIndex) {
								loadMoreFamilyMembers();
								return;
							}
						}
						if (usedPhotos.size() >= maxUsed) {
							usedPhotos.remove(0);
						}
						usedPhotos.add(photo);

                        counter = 0;
						listener.onMediaLoaded(photo);
                    } catch (Exception e) {
                        Log.e(getClass().getName(), "Error loading random media from table", e);
                        counter = 0;
                        listener.onMediaLoaded(null);
                    }

                } else {
                    counter = 0;
                    listener.onMediaLoaded(null);
                }
            }
        } else {
            int index = random.nextInt(photos.size());
            int origIndex = index;
            photo = photos.get(index);
            while (usedPhotos.contains(photo)) {
                index++;
                if (index >= photos.size()) index = 0;
                photo = photos.get(index);
                //-- stop if we've used all of these images
                if (index == origIndex) {
                    loadMoreFamilyMembers();
                    return;
                }
            }

            if (usedPhotos.size() >= maxUsed) {
                usedPhotos.remove(0);
            }
            usedPhotos.add(photo);

            listener.onMediaLoaded(photo);
        }
    }

    public class FamilyLoaderListener implements FamilyLoaderTask.Listener {
        @Override
        public void onComplete(ArrayList<LittlePerson> family) {
            int counter =0;
            for (LittlePerson p : family) {
				if (!familyLoaderQueue.contains(p)) familyLoaderQueue.add(p);
                if (!noPhotos.contains(p) && !people.contains(p)) {
					if (p.isHasMedia()==null || p.isHasMedia()) {
                    	people.add(p);
                    	counter++;
					}
					else {
						noPhotos.add(p);
					}
                }
            }

            backgroundLoadIndex++;

            //-- no pictures try again
            if (counter==0 && people.size()<3) {
                loadMoreFamilyMembers();
            } else {
                loadRandomImage();
            }
        }

        @Override
        public void onStatusUpdate(String message) {

        }
    }

    public interface RandomMediaListener {
        public void onMediaLoaded(Media media);
    }
}
