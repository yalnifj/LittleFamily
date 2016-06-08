package com.yellowforktech.littlefamilytree.remote.familygraph;

import com.familygraph.android.FamilyGraphError;
import com.yellowforktech.littlefamilytree.remote.phpgedview.FamilyHolder;
import com.yellowforktech.littlefamilytree.remote.phpgedview.GedcomParser;

import org.gedcomx.common.URI;
import org.gedcomx.conclusion.Date;
import org.gedcomx.conclusion.Fact;
import org.gedcomx.conclusion.Gender;
import org.gedcomx.conclusion.Name;
import org.gedcomx.conclusion.NameForm;
import org.gedcomx.conclusion.NamePart;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.PlaceReference;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;
import org.gedcomx.source.SourceReference;
import org.gedcomx.types.FactType;
import org.gedcomx.types.GenderType;
import org.gedcomx.types.NamePartType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jfinlay on 6/6/2016.
 */
public class JSONConverter {

    public Person convertJSONPerson(JSONObject json) throws JSONException, FamilyGraphError {
        checkError(json);
        Person person = new Person();
        person.setId(json.getString("id"));
        String gen = json.getString("gender");
        Gender gender = new Gender();
        gender.setKnownType(GenderType.Unknown);
        if ("M".equals(gen)) {
            gender.setKnownType(GenderType.Male);
        } else if ("F".equals(gen)) {
            gender.setKnownType(GenderType.Female);
        }
        person.setGender(gender);

        if (json.has("is_alive")) {
            person.setLiving(json.getBoolean("is_alive"));
        }

        person.addLink(new Link("link", new URI(json.getString("link"))));

        Name name = new Name();
        NameForm form = new NameForm();
        form.setFullText(json.getString("name").replaceAll(" \\([\\w\\s]*\\)", ""));
        if (json.has("first_name")) {
            NamePart part = new NamePart();
            part.setKnownType(NamePartType.Given);
            part.setValue(json.getString("first_name"));
            form.addPart(part);
        }
        if (json.has("last_name")) {
            NamePart part = new NamePart();
            part.setKnownType(NamePartType.Surname);
            part.setValue(json.getString("last_name"));
            form.addPart(part);
        }
        if (json.has("name_prefix")) {
            NamePart part = new NamePart();
            part.setKnownType(NamePartType.Prefix);
            part.setValue(json.getString("name_prefix"));
            form.addPart(part);
        }
        if (json.has("name_suffix")) {
            NamePart part = new NamePart();
            part.setKnownType(NamePartType.Suffix);
            part.setValue(json.getString("name_suffix"));
            form.addPart(part);
        }
        name.addNameForm(form);
        person.addName(name);

        if (json.has("nickname")) {
            Name nickname = new Name();
            form = new NameForm();
            NamePart part = new NamePart();
            part.setKnownType(NamePartType.Given);
            part.setValue(json.getString("nickname"));
            form.addPart(part);
            name.addNameForm(form);
            person.addName(nickname);
        }

        if (json.has("personal_photo")) {
            JSONObject photo = json.getJSONObject("personal_photo");
            String photo_id = photo.getString("id");
            SourceReference sd = new SourceReference();
            Link link = new Link();
            link.setRel("image");
            URI uri = new URI(photo_id);
            link.setHref(uri);
            sd.addLink(link);
            person.addMedia(sd);
        }

        return person;
    }

    public void processEvents(JSONObject json, Person person) throws FamilyGraphError, JSONException {
        checkError(json);

        JSONArray facts = json.getJSONArray("data");
        for (int i=0; i<facts.length(); i++) {
            JSONObject factJ = facts.getJSONObject(i);
            String eventType = factJ.getString("event_type");

            Fact fact = new Fact();
            FactType type = GedcomParser.factMap.get(eventType);
            if (type==null) {
                type = FactType.OTHER;
                fact.setType(new URI(eventType));
            }
            fact.setKnownType(type);
            if (factJ.has("header")) {
                fact.setValue(factJ.getString("header"));
            }
            if (factJ.has("date")) {
                JSONObject dateJ = factJ.getJSONObject("date");
                Date date = new Date();
                date.setOriginal("+"+dateJ.getString("date"));
                date.setFormal(dateJ.getString("gedcom"));
                fact.setDate(date);
            }
            if (factJ.has("place")) {
                PlaceReference place = new PlaceReference();
                place.setOriginal(factJ.getString("place"));
                fact.setPlace(place);
            }

            person.addFact(fact);
        }
    }

    public SourceDescription convertMedia(JSONObject media) throws JSONException, FamilyGraphError {
        checkError(media);

        SourceDescription sd = new SourceDescription();
        sd.setId(media.getString("id"));

        if (media.has("is_personal_photo")) {
            if (media.getBoolean("is_personal_photo")) {
                sd.setSortKey("1");
            }
        }

        Link link = new Link();
        if (media.getString("type").equals("photo")) {
            link.setRel("image");
        } else {
            link.setRel(media.getString("type"));
        }

        URI uri = new URI(media.getString("url"));
        link.setHref(uri);
        sd.addLink(link);
        return sd;
    }

    public FamilyHolder convertFamily(JSONObject json) throws JSONException, FamilyGraphError {
        checkError(json);

        FamilyHolder family = new FamilyHolder();
        family.setId(json.getString("id"));
        if (json.has("husband")) {
            family.addParent(new Link("HUSB", new URI(json.getJSONObject("husband").getString("id"))));
        }
        if (json.has("wife")) {
            family.addParent(new Link("WIFE", new URI(json.getJSONObject("wife").getString("id"))));
        }
        if (json.has("children")) {
            JSONArray jchildren = json.getJSONArray("children");
            for (int c=0; c<jchildren.length(); c++) {
                JSONObject child = jchildren.getJSONObject(c);
                family.addChild(new Link("CHIL", new URI(child.getJSONObject("child").getString("id"))));
            }
        }
        return family;
    }

    public void checkError(JSONObject json) throws JSONException, FamilyGraphError {
        if (json.has("error")) {
            int code = json.getInt("error");
            String message = json.getString("error_description");
            throw new FamilyGraphError(message, "API Error", code);
        }
    }
}
