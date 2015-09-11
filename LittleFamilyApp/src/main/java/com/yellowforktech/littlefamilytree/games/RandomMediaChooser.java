package com.yellowforktech.littlefamilytree.games;

import android.util.Log;

import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.activities.tasks.FamilyLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.MemoriesLoaderTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.Media;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by Parents on 8/16/2015.
 */
public class RandomMediaChooser implements MemoriesLoaderTask.Listener {
    public List<LittlePerson> people;
    private LittlePerson selectedPerson;
    private Media photo;
    private List<Media> usedPhotos;
    private List<LittlePerson> noPhotos;
    private LittleFamilyActivity activity;
    private RandomMediaListener listener;

    private int backgroundLoadIndex = 0;

    private int maxTries = 20;
    private int maxUsed = 20;
	
	private Random random;

    public RandomMediaChooser(List<LittlePerson> people, LittleFamilyActivity activity, RandomMediaListener listener) {
        this.people = people;
        this.activity = activity;
        this.listener = listener;

        usedPhotos = new ArrayList<>(maxUsed);
        noPhotos = new ArrayList<>();
		
		random = new Random();

        Iterator<LittlePerson> iterator = this.people.iterator();
        while(iterator.hasNext()) {
            LittlePerson person = iterator.next();
            if (person.isHasMedia()!=null && person.isHasMedia()==false) {
                noPhotos.add(person);
                iterator.remove();
            }
        }
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

    public void setPeople(List<LittlePerson> people) {
        this.people = people;
    }

    public void loadRandomImage() {
        if (people != null && people.size() > 0) {
            Random rand = new Random();
            selectedPerson = people.get(rand.nextInt(people.size()));
            MemoriesLoaderTask task = new MemoriesLoaderTask(this, activity);
            task.execute(selectedPerson);
        }
    }

    public void loadMoreFamilyMembers() {
        if (backgroundLoadIndex < maxTries) {
            FamilyLoaderTask task = new FamilyLoaderTask(new FamilyLoaderListener(), activity);
            if (selectedPerson==null) {
                if (people.size()>0) {
                    selectedPerson = people.get(0);
                } else {
                    selectedPerson = noPhotos.get(0);
                }
            }
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
            if (backgroundLoadIndex < maxTries) {
                people.remove(selectedPerson);
                noPhotos.add(selectedPerson);
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

						listener.onMediaLoaded(photo);
                    } catch (Exception e) {
                        Log.e(getClass().getName(), "Error loading random media from table", e);
                        listener.onMediaLoaded(null);
                    }

                } else {
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
                if (!noPhotos.contains(p) && !people.contains(p)) {
                    people.add(p);
                    counter++;
                }
            }

            backgroundLoadIndex++;

            //-- no pictures add all the people
            if (counter==0) {
                if (people.size()<2) {
                    people.addAll(family);
                }
                if (people.size()>1) {
                    LittlePerson current = selectedPerson;
                    Random rand = new Random();
                    selectedPerson = people.get(rand.nextInt(people.size()));
                    while (current == selectedPerson) {
                        selectedPerson = people.get(rand.nextInt(people.size()));
                    }
                }
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
