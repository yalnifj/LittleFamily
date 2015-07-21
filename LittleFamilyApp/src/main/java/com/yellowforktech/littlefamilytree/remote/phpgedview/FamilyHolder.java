package com.yellowforktech.littlefamilytree.remote.phpgedview;

import org.gedcomx.conclusion.Fact;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 4/24/2015.
 */
public class FamilyHolder {
    private String id;
    private List<Fact> facts;
    private List<Relationship> relationships;
    private List<SourceReference> media;
    private List<Link> parents;
    private List<Link> children;

    public FamilyHolder() {
        facts = new ArrayList<>(0);
        relationships = new ArrayList<>(0);
        media = new ArrayList<>(0);
        parents = new ArrayList<>(0);
        children = new ArrayList<>(0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

    public List<SourceReference> getMedia() {
        return media;
    }

    public void setMedia(List<SourceReference> media) {
        this.media = media;
    }

    public List<Link> getParents() {
        return parents;
    }

    public void setParents(List<Link> parents) {
        this.parents = parents;
    }

    public List<Link> getChildren() {
        return children;
    }

    public void setChildren(List<Link> children) {
        this.children = children;
    }

    public void addFact(Fact fact) {
        facts.add(fact);
    }

    public void addRelationship(Relationship r) {
        relationships.add(r);
    }

    public void addMedia(SourceReference sr) {
        media.add(sr);
    }

    public void addParent(Link link) {
        parents.add(link);
    }

    public void addChild(Link link) {
        children.add(link);
    }
}
