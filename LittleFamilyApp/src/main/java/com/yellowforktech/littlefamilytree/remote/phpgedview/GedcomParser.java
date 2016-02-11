package com.yellowforktech.littlefamilytree.remote.phpgedview;

import android.util.Log;

import org.gedcomx.atom.Entry;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jfinlay on 4/24/2015.
 */
public class GedcomParser {

    private Map<String, FactType> factMap;
    //private Map<FactType, String> typeMap;

    public GedcomParser(){
        factMap = new HashMap<>();
        factMap.put("ADOP", FactType.Adoption);
        factMap.put("ANUL", FactType.Annulment);
        factMap.put("BAPM", FactType.Baptism);
        factMap.put("BARM", FactType.BarMitzvah);
        factMap.put("BASM", FactType.BatMitzvah);
        factMap.put("BIRT", FactType.Birth);
        factMap.put("BLES", FactType.Blessing);
        factMap.put("BURI", FactType.Burial);
        factMap.put("CAST", FactType.Caste);
        factMap.put("CENS", FactType.Census);
        factMap.put("CHR", FactType.Christening);
        factMap.put("CHRA", FactType.AdultChristening);
        factMap.put("CONF", FactType.Confirmation);
        factMap.put("CREM", FactType.Cremation);
        factMap.put("DEAT", FactType.Death);
        factMap.put("DIV", FactType.Divorce);
        factMap.put("DIVF", FactType.DivorceFiling);
        factMap.put("EDUC", FactType.Education);
        factMap.put("EMIG", FactType.Emigration);
        factMap.put("ENGA", FactType.Engagement);
        factMap.put("NATI", FactType.Ethnicity);
        factMap.put("FCOM", FactType.FirstCommunion);
        factMap.put("IMMI", FactType.Immigration);
        factMap.put("MARR", FactType.Marriage);
        factMap.put("MARB", FactType.MarriageBanns);
        factMap.put("MARC", FactType.MarriageContract);
        factMap.put("MARL", FactType.MarriageLicense);
        factMap.put("NATI", FactType.Nationality);
        factMap.put("IDNO", FactType.NationalId);
        factMap.put("NATU", FactType.Naturalization);
        factMap.put("OCCU", FactType.Occupation);
        factMap.put("ORDI", FactType.Ordination);
        factMap.put("DSCR", FactType.PhysicalDescription);
        factMap.put("RELI", FactType.Religion);
        factMap.put("PROB", FactType.Probate);
        factMap.put("PROP", FactType.Property);
        factMap.put("WILL", FactType.Will);
        factMap.put("RETI", FactType.Retirement);
        factMap.put("RESI", FactType.Residence);
    }

    public Person parsePerson(String gedcom) throws GedcomParseException {
        Person person = null;
        String[] lines = gedcom.split("(\r?\n)+");
        if (!lines[0].matches("0 @\\w+@ INDI")) {
            throw new GedcomParseException("Not a valid INDI gedcom record");
        }
        person = new Person();
        String xref = lines[0].substring(3, lines[0].lastIndexOf('@'));
        person.setId(xref);

        //-- create a list of facts
        List<List<String>> level2s = new ArrayList<>();
        List<String> assertion = null;
        for(int s=1; s<lines.length; s++) {
            String line = lines[s];
            if (line.startsWith("1 ")) {
                if (assertion !=null ) level2s.add(assertion);
                assertion = new ArrayList<>();
                assertion.add(line);
            }
            else if (assertion!=null) {
                assertion.add(line);
            }
        }
        if (assertion.size()>0) {
            level2s.add(assertion);
        }
        boolean hasdeath = false;
        //-- parse each fact
        for(List<String> a : level2s) {
            String line2 = a.get(0);
            String[] parts = line2.split(" ", 3);
            if (parts[1].equals("NAME")) {
                Name name = parseName(a);
                if (person.getNames()==null || person.getNames().size()==0) {
                    name.setPreferred(true);
                }
                person.addName(name);
            }
            else if (parts[1].equals("SEX")) {
                Gender gender = new Gender();
                char g = parts[2].charAt(0);
                if (g=='M') gender.setKnownType(GenderType.Male);
                else if (g=='F') gender.setKnownType(GenderType.Female);
                else gender.setKnownType(GenderType.Unknown);
                person.setGender(gender);
            }
            else if (parts[1].equals("SOUR")) {
                // TODO
            }
            else if (parts[1].equals("NOTE")) {
                // TODO
            }
            else if (parts[1].equals("OBJE")) {
                SourceReference sd = parseMedia(a);
                person.addMedia(sd);
            }
            else if (parts[1].equals("FAMS")) {
                person.addLink("FAMS", new URI(parts[2]));
            }
            else if (parts[1].equals("FAMC")) {
                person.addLink("FAMC", new URI(parts[2]));
            }
            else if (parts[1].equals("CHAN")) {
                Entry entry = parseEntry(a);
                person.setTransientProperty("CHAN", entry.getUpdated());
            }
            else if (factMap.get(parts[1])!=null) {
                Fact fact = parseFact(a);
                person.addFact(fact);
                if (fact.getKnownType()==FactType.Death || fact.getKnownType()==FactType.Burial || fact.getKnownType()==FactType.Cremation) {
                    hasdeath = true;
                }
            }
        }

        if (hasdeath) person.setLiving(false);

        return person;
    }

    public FamilyHolder parseFamily(String gedcom) throws GedcomParseException{
        FamilyHolder family = null;
        String[] lines = gedcom.split("(\r?\n)+");
        if (!lines[0].matches("0 @\\w+@ FAM")) {
            throw new GedcomParseException("Not a valid FAM gedcom record");
        }
        family = new FamilyHolder();
        String xref = lines[0].substring(3, lines[0].lastIndexOf('@'));
        family.setId(xref);

        //-- create a list of facts
        List<List<String>> level2s = new ArrayList<>();
        List<String> assertion = null;
        for(int s=1; s<lines.length; s++) {
            String line = lines[s];
            if (line.startsWith("1 ")) {
                if (assertion !=null ) level2s.add(assertion);
                assertion = new ArrayList<>();
                assertion.add(line);
            }
            else if (assertion!=null) {
                assertion.add(line);
            }
        }
        //-- parse each fact
        for(List<String> a : level2s) {
            String line2 = a.get(0);
            String[] parts = line2.split(" ", 3);
            if (parts[1].equals("SOUR")) {
                // TODO
            }
            else if (parts[1].equals("NOTE")) {
                // TODO
            }
            else if (parts[1].equals("OBJE")) {
                SourceReference sd = parseMedia(a);
                family.addMedia(sd);
            }
            else if (parts[1].equals("HUSB")) {
                family.addParent(new Link("HUSB", new URI(parts[2])));
            }
            else if (parts[1].equals("WIFE")) {
                family.addParent(new Link("WIFE", new URI(parts[2])));
            }
            else if (parts[1].equals("CHIL")) {
                family.addChild(new Link("CHIL", new URI(parts[2])));
            }
            else if (factMap.get(parts[1])!=null) {
                Fact fact = parseFact(a);
                family.addFact(fact);
            }
        }

        return family;
    }

    public SourceDescription parseObje(String gedcom, String baseUrl) throws GedcomParseException{
        String[] lines = gedcom.split("(\r?\n)+");
        if (!lines[0].matches("0 @\\w+@ OBJE")) {
            throw new GedcomParseException("Not a valid OBJE gedcom record");
        }
        SourceDescription sd = new SourceDescription();
        String xref = lines[0].substring(3, lines[0].lastIndexOf('@'));
        sd.setId(xref);
        for(int s=1; s<lines.length; s++) {
            String line = lines[s];
            String[] ps = line.split(" ", 3);
            if ("FILE".equals(ps[1])) {
                Link link = new Link();
                String mediaPath = ps[2].trim();
                String[] paths = mediaPath.split("\\.");
                String ext = paths[paths.length-1].toLowerCase();
                if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("gif") || ext.equals("png")) {
                    link.setRel("image");
                } else if (ext.equals("pdf")) {
                    link.setRel("pdf");
                } else {
                    link.setRel("other");
                }
                mediaPath = mediaPath.replaceAll(" ", "%20");
                if (!mediaPath.startsWith("http") && !mediaPath.startsWith("ftp:")) {
                    mediaPath = baseUrl + mediaPath;
                }
                URI uri = new URI(mediaPath);
                link.setHref(uri);
                sd.addLink(link);
            }
            if ("_PRIM".equals(ps[1]) && !"N".equals(ps[2])) {
                sd.setSortKey("1");
            }
        }
        return sd;
    }

    public Name parseName(List<String> lines) {
        Name name = new Name();
        NameForm form = null;
        String wholeName = lines.get(0).substring(7);
        for(int s=1; s<lines.size(); s++) {
            if (form==null) {
                form = new NameForm();
                form.setFullText(wholeName.replaceAll("/", ""));
            }
            String line = lines.get(s);
            String[] parts = line.split(" ", 3);
            if ("GIVN".equals(parts[1])) {
                NamePart part = new NamePart();
                part.setKnownType(NamePartType.Given);
                part.setValue(parts[2]);
                form.addPart(part);
            }
            if ("SURN".equals(parts[1])) {
                NamePart part = new NamePart();
                part.setKnownType(NamePartType.Surname);
                part.setValue(parts[2]);
                form.addPart(part);
            }
            if ("NPFX".equals(parts[1])) {
                NamePart part = new NamePart();
                part.setKnownType(NamePartType.Prefix);
                part.setValue(parts[2]);
                form.addPart(part);
            }
            if ("NSFX".equals(parts[1])) {
                NamePart part = new NamePart();
                part.setKnownType(NamePartType.Suffix);
                part.setValue(parts[2]);
                form.addPart(part);
            }
            //-- TODO parse SOUR, NOTE, etc.
        }

        if (form!=null) name.addNameForm(form);
        else {
            Pattern p = Pattern.compile("(.*)/(.*)/(.*)");
            Matcher m = p.matcher(wholeName);
            if (m.find()) {
                form = new NameForm();
                form.setFullText(wholeName.replaceAll("/", ""));
                String givn = m.group(1).trim();
                if (!givn.isEmpty()) {
                    NamePart part = new NamePart();
                    part.setKnownType(NamePartType.Given);
                    part.setValue(givn);
                    form.addPart(part);
                }
                String surn = m.group(2).trim();
                if (!surn.isEmpty()) {
                    NamePart part = new NamePart();
                    part.setKnownType(NamePartType.Surname);
                    part.setValue(surn);
                    form.addPart(part);
                }
                String sufx = m.group(2).trim();
                if (!sufx.isEmpty()) {
                    NamePart part = new NamePart();
                    part.setKnownType(NamePartType.Suffix);
                    part.setValue(sufx);
                    form.addPart(part);
                }
                name.addNameForm(form);
            }
        }
        return name;
    }

    public SourceReference parseMedia(List<String> lines) {
        SourceReference sd = new SourceReference();
        String[] parts = lines.get(0).split(" ", 3);
        if (parts[2]!=null && !parts[2].isEmpty()) {
            Link link = new Link();
            link.setRel("image");
            URI uri = new URI(parts[2]);
            link.setHref(uri);
            sd.addLink(link);
        }
        for (String line : lines) {
            String[] ps = line.split(" ", 3);
            if ("FILE".equals(ps[1])) {
                Link link = new Link();
                link.setRel("image");
                URI uri = new URI(ps[2]);
                link.setHref(uri);
                sd.addLink(link);
            }
        }

        return sd;
    }

    public Fact parseFact(List<String> lines) {
        Fact fact = new Fact();
        String[] parts = lines.get(0).split(" ", 3);
        FactType type = factMap.get(parts[1]);
        if (type==null) {
            type = FactType.OTHER;
        }
        fact.setKnownType(type);
        if (parts.length>2 && parts[2]!=null && !parts[2].isEmpty()) {
            fact.setValue(parts[2]);
        }
        for (int s=1; s<lines.size(); s++) {
            String line = lines.get(s);
            String[] ps = line.split(" ", 3);
            if (ps[0].equals("2")) {
                if (ps[1].equals("DATE") && fact.getDate() == null) {
                    Date date = new Date();
                    date.setOriginal(ps[2]);
                    fact.setDate(date);
                }
                if (ps[1].equals("PLAC") && fact.getPlace() == null) {
                    PlaceReference place = new PlaceReference();
                    place.setOriginal(ps[2]);
                    fact.setPlace(place);
                }
                if (ps[1].equals("TYPE") && fact.getType()==null) {
                    fact.setType(new URI(ps[2]));
                }
            }
        }
        return fact;
    }

    public Entry parseEntry(List<String> lines) {
        Entry entry = null;
        Calendar cal = Calendar.getInstance();
        boolean hasDate = false;
        for (int s=1; s<lines.size(); s++) {
            String line = lines.get(s);
            String[] ps = line.split(" ", 3);
            if (ps[0].equals("2")) {
                if (ps[1].equals("DATE")) {
                    DateFormat df = new SimpleDateFormat("dd MMM yyyy");
                    try {
                        java.util.Date date = df.parse(ps[2]);
                        cal.setTime(date);
                        hasDate = true;
                    } catch (ParseException e) {
                        Log.e("GedcomParser", "Error parsing date "+ps[2], e);
                    }
                }
                if (ps[1].equals("TIME")) {
                    DateFormat df = new SimpleDateFormat("HH:mm:ss");
                    try {
                        java.util.Date time = df.parse(ps[2]);
                        Calendar cal2 = Calendar.getInstance();
                        cal2.setTime(time);
                        cal.set(Calendar.HOUR, cal2.get(Calendar.HOUR));
                        cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
                        cal.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
                    } catch (ParseException e) {
                        Log.e("GedcomParser", "Error parsing date "+ps[2], e);
                    }
                }
            }
        }
        if (hasDate) {
            entry = new Entry();
            entry.setUpdated(cal.getTime());
        }
        return entry;
    }
}
