/*

 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.glasnost.orika.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.property.PropertyResolver;
import ma.glasnost.orika.property.PropertyResolverStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScoringClassMapBuilder is an extension of the basic ClassMapBuilder that
 * attempts to compute a best-fit matching of all properties (at every level
 * of nesting) of one type to another, based on various metrics used to measure
 * a given property match.<br><br>
 * 
 * Since this builder generates mappings based on scoring matches, it cannot always
 * guess the correct mappings; be sure to test and double-check the mappings
 * generated to assure they match expectations.<br><br>
 * 
 * Note: levenshtein distance implementation is pulled from code found in
 * Apache Commons Lang <em>org.apache.commons.lang.StringUtils</em>, which is based on
 * the implementation provided by Chas Emerick 
 * <a href="http://www.merriampark.com/ldjava.htm">http://www.merriampark.com/ldjava.htm</a>
 * 
 * @author matt.deboer@gmail.com
 * @param <A>
 * @param <B>
 */
public class ScoringClassMapBuilder<A, B> extends ClassMapBuilder<A, B> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoringClassMapBuilder.class);
    
    private final PropertyMatchingWeights matchingWeights;
    
    /**
     * PropertyMatchingWeights  is a class used to describe how different
     * matching scenarios should be weighted when computing a match
     * score for a set of properties.
     * 
     * @author matt.deboer@gmail.com
     *
     */
    public static final class PropertyMatchingWeights {
        
        private static final double MIN_WEIGHT = 0.0;
        private static final double MAX_WEIGHT = 1.0;
        
        private double nestedDepth = MAX_WEIGHT / 2.0;
        private double unmatchedWords = MAX_WEIGHT / 2.0;
        private double editDistance = MAX_WEIGHT / 2.0;
        private double containsName = MAX_WEIGHT / 2.0;
        private double typeMatch = MAX_WEIGHT / 2.0;
        private double commonWordCount = MAX_WEIGHT / 2.0;
        private double minimumScore = MAX_WEIGHT / 2.0;
        
        /**
         * @return the weight associated with the number of words found in common
         * between two property expressions
         */
        public double commonWordCount() {
            return commonWordCount;
        }
        /**
         * Set the weight associated with the number of words found in common
         * between two property expressions
         * 
         * @param weight the weight associated with the number of words found in common
         * @return this instance of PropertyMatchingWeights
         * between two property expressions
         */
        public PropertyMatchingWeights commonWordCount(double weight) {
            validateWeight(weight);
            this.commonWordCount = weight;
            return this;
        }
        /**
         * @return the weight associated with one property containing the
         * entire name of another property
         */
        public double containsName() {
            return containsName;
        }
        /**
         * Set the weight associated with one property containing the
         * entire name of another property.
         * 
         * @param weight the weight associated with one property containing the
         * entire name of another property
         * @return this instance of PropertyMatchingWeights
         */
        public PropertyMatchingWeights containsName(double weight) {
            validateWeight(weight);
            this.containsName = weight;
            return this;
        }
        /**
         * @return the weight associated with one property matching the type of the other
         */
        public double typeMatch() {
            return typeMatch;
        }
        /**
         * Set the weight associated with one property matching the type of the other
         * 
         * @param weight the weight associated with one property matching the type of the other
         * @return this instance of PropertyMatchingWeights
         */
        public PropertyMatchingWeights typeMatch(double weight) {
            validateWeight(weight);
            this.typeMatch = weight;
            return this;
        }
        /**
         * @return the weight modifier associated with a property word's edit distance based on
         * it's nesting depth
         */
        public double nestedDepth() {
            return nestedDepth;
        }
        /**
         * Set the weight modifier associated with a property word's edit distance based on
         * it's nesting depth; higher values here causes the matching to be more focused toward
         * the final name of a nested property, lower values focus on the entire name more evenly
         * 
         * @param weight the weight modifier associated with a property word's edit distance based on
         * it's nesting depth
         * @return this instance of PropertyMatchingWeights
         */
        public PropertyMatchingWeights nestedDepth(double weight) {
            validateWeight(weight);
            this.nestedDepth = weight;
            return this;
        }
        
        /**
         * @return the weight associated with the number of unmatched words between two property expressions
         */
        public double unmatchedWords() {
            return unmatchedWords;
        }
        
        /**
         * Set the weight associated with the number of unmatched words between two property expressions
         * 
         * @param weight the weight associated with the number of unmatched words between two property expressions
         * @return this instance of PropertyMatchingWeights
         */
        public PropertyMatchingWeights unmatchedWords(double weight) {
            validateWeight(weight);
            this.unmatchedWords = weight;
            return this;
        }
        /**
         * @return the weight associated with the edit distance between words in two property expressions
         */
        public double editDistance() {
            return editDistance;
        }
        /**
         * Set the weight associated with the edit distance between words in two property expressions
         * 
         * 
         * @param weight the weight associated with the edit distance between words in two property expressions
         * @return this instance of PropertyMatchingWeights
         */
        public PropertyMatchingWeights editDistance(double weight) {
            validateWeight(weight);
            this.editDistance = weight;
            return this;
        }
        /**
         * @return the weight applied to the minimum score needed to accept a given match
         */
        public double minimumScore() {
            return minimumScore;
        }
        
        /**
         * Set the weight applied to the minimum score needed to accept a given match; setting higher
         * values makes the matching more restrictive, lower scores make matching more lenient.
         * 
         * @param weight the weight applied to the minimum score needed to accept a given match
         * @return this instance of PropertyMatchingWeights
         */
        public PropertyMatchingWeights minimumScore(double weight) {
            validateWeight(weight);
            this.minimumScore = weight;
            return this;
        }
        private void validateWeight(double weight) {
            if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
                throw new IllegalArgumentException("weights should be between " + MIN_WEIGHT + " and " + MAX_WEIGHT);
            }
        }
    }
    
    
    /**
     * Constructs a new instance of ScoringClassMapBuilder, using the provided PropertyMatchingWeights
     * to adjust the overall scoring of how properties are matched.
     * 
     * @param aType
     * @param bType
     * @param propertyResolver
     * @param defaults
     */
    protected ScoringClassMapBuilder(Type<A> aType, Type<B> bType, MapperFactory mapperFactory, PropertyResolverStrategy propertyResolver,
            DefaultFieldMapper[] defaults, PropertyMatchingWeights matchingWeights) {
        super(aType, bType, mapperFactory, propertyResolver, defaults);
        this.matchingWeights = matchingWeights;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.metadata.ClassMapBuilder#byDefault(ma.glasnost.
     * orika.DefaultFieldMapper[])
     */
    public ClassMapBuilder<A, B> byDefault(MappingDirection direction, DefaultFieldMapper... withDefaults) {
        
        DefaultFieldMapper[] defaults;
        if (withDefaults.length == 0) {
            defaults = getDefaultFieldMappers();
        } else {
            defaults = withDefaults;
        }
        /*
         * For our custom 'byDefault' method, we're going to try and match
         * fields by their Levenshtein distance
         */
        PriorityQueue<FieldMatchScore> matchScores = new PriorityQueue<FieldMatchScore>();
        
        Map<String, Property> propertiesForA = getPropertyExpressions(getAType());
        Map<String, Property> propertiesForB = getPropertyExpressions(getBType());
        
        for (final Entry<String, Property> propertyA : propertiesForA.entrySet()) {
            if (!propertyA.getValue().getName().equals("class")) {
                for (final Entry<String, Property> propertyB : propertiesForB.entrySet()) {
                    if (!propertyB.getValue().getName().equals("class")) {
                        FieldMatchScore matchScore = new FieldMatchScore(propertyA.getValue(), propertyB.getValue(), matchingWeights);
                        matchScores.add(matchScore);
                    }
                }
            }
        }
        
        Set<String> unmatchedFields = new LinkedHashSet<String>(this.getPropertiesForTypeA());
        unmatchedFields.remove("class");
        
        for (FieldMatchScore score : matchScores) {
            
            if (!this.getMappedPropertiesForTypeA().contains(score.propertyA.getExpression())
                    && !this.getMappedPropertiesForTypeB().contains(score.propertyB.getExpression())) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("\n" + score.toString());
                }
                if (score.meetsMinimumScore()) {
                    fieldMap(score.propertyA.getExpression(), score.propertyB.getExpression()).direction(direction).add();
                    unmatchedFields.remove(score.propertyA.getExpression());
                }
            }
        }
        
        /*
         * Apply any default field mappers to the unmapped fields
         */
        for (String propertyNameA : unmatchedFields) {
            Property prop = resolvePropertyForA(propertyNameA);
            for (DefaultFieldMapper defaulter : defaults) {
                String suggestion = defaulter.suggestMappedField(propertyNameA, prop.getType());
                if (suggestion != null && getPropertiesForTypeB().contains(suggestion)) {
                    if (!getMappedPropertiesForTypeB().contains(suggestion)) {
                        fieldMap(propertyNameA, suggestion).direction(direction).add();
                    }
                }
            }
        }
        
        return this;
    }
    
    /**
     * @author mattdeboer
     *
     */
    public static class Factory extends ClassMapBuilderFactory {
        
        private PropertyMatchingWeights matchingWeights;
        
        /**
         * Constructs a new Factory for ScoringClassMapBuilder instances
         */
        public Factory() {
            matchingWeights = new PropertyMatchingWeights();
        }
        
        /**
         * Constructs a new Factory for ScoringClassMapBuilder instances
         * 
         * @param matchingWeights the weights used to control the scorin on ScoringClassMapBuilder instances
         * created by this factory
         */
        public Factory(PropertyMatchingWeights matchingWeights) {
            this.matchingWeights = matchingWeights;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * ma.glasnost.orika.metadata.ClassMapBuilderFactory#newClassMapBuilder
         * (ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type,
         * ma.glasnost.orika.property.PropertyResolverStrategy,
         * ma.glasnost.orika.DefaultFieldMapper[])
         */
        @Override
        protected <A, B> ClassMapBuilder<A, B> newClassMapBuilder(Type<A> aType, Type<B> bType, MapperFactory mapperFactory,
                PropertyResolverStrategy propertyResolver, DefaultFieldMapper[] defaults) {
            
            return new ScoringClassMapBuilder<A, B>(aType, bType, mapperFactory, propertyResolver, defaults, matchingWeights);
        }
        
    }
    
    /**
     * FieldMatchScore is used to score the match of a pair of property expressions
     * 
     * @author matt.deboer@gmail.com
     * 
     */
    public static class FieldMatchScore implements Comparable<FieldMatchScore> {
        
        private static final List<String> IGNORED_WORDS = Arrays.asList("with","this","that","an","a","of","the");
        /*
         * TODO: static for now; should probably be computed
         */
        private static final double MAX_POSSIBLE_SCORE = 50.0;
        
        private final PropertyMatchingWeights matchingWeights;
        
        private boolean contains;
        private boolean containsIgnoreCase;
        private double typeMatch;
        private Property propertyA;
        private Property propertyB;
        private int hashCode;
        private double commonWordCount;
        private double avgWordCount;
        private double wordMatchScore;
        private double score;
        private double typeMatchScore;
        private double commonWordsScore;
        private double containsScore;
        
        /**
         * Constructs a new FieldMatchScore based on the provided pair of properties, with scoring modified by
         * the provided PropertyMatchingWeights
         * 
         * @param propertyA
         * @param propertyB
         * @param matchingWeights
         */
        public FieldMatchScore(Property propertyA, Property propertyB, PropertyMatchingWeights matchingWeights) {
            
            this.matchingWeights = matchingWeights;
            this.propertyA = propertyA;
            this.propertyB = propertyB;
            
            String propertyALower = propertyA.getName().toLowerCase();
            String propertyBLower = propertyB.getName().toLowerCase();
            
            List<List<String>> aWords = splitIntoLowerCaseWords(propertyA.getExpression());
            List<List<String>> bWords = splitIntoLowerCaseWords(propertyB.getExpression());
            
            aWords.removeAll(IGNORED_WORDS);
            bWords.removeAll(IGNORED_WORDS);
            
            Set<String> commonWords = intersection(aWords,bWords);
            
            this.avgWordCount = (aWords.size() + bWords.size()) / 2.0;
            
            this.commonWordCount = commonWords.size();
            this.wordMatchScore = computeWordMatchScore(aWords, bWords);
            
            this.contains = propertyA.getName().contains(propertyB.getName()) || propertyB.getName().contains(propertyA.getName());
            this.containsIgnoreCase = contains || propertyALower.contains(propertyBLower) || propertyBLower.contains(propertyALower);
            
            if ((propertyA.isMultiOccurrence() && !propertyB.isMultiOccurrence())
                    || (!propertyA.isMultiOccurrence() && propertyB.isMultiOccurrence())) {
                this.typeMatch = Double.NEGATIVE_INFINITY;
            } else if (propertyA.getType().isAssignableFrom(propertyB.getType()) 
                    || propertyB.getType().isAssignableFrom(propertyA.getType())){
                this.typeMatch = 1.0;
            } else {
                this.typeMatch = 0.0;
            }
            
            
            computeOverallScore();
            
            this.hashCode = computeHashCode();
        }
        
        public String toString() {
            return 
                "[" + propertyA.getExpression() + ", " + propertyB.getExpression() + "] {\n" +
                "   wordMatchScore: " + wordMatchScore + "\n" +
                "   commonWordScore: " + commonWordsScore + "\n" +
                "   containsScore: " + containsScore + "\n" +
                "   typeMatchScore: " + typeMatchScore + "\n" +
                "   ------------------- \n" +
                "   total: " + score + "\n" +
                "}";
        }
        
        private <T> Set<T> intersection(List<List<T>> setA, List<List<T>> setB) {
            Set<T> intersection = flatten(setA);
            Set<T> temp = flatten(setB);
            intersection.retainAll(temp);
            return intersection;
        }

        private <T> Set<T> flatten(List<List<T>> aWords) {
            Set<T> set = new LinkedHashSet<T>();
            for (List<T> collection: aWords) {
                for (T item: collection) {
                    set.add(item);
                }
            }
            return set;
        }
        
        /**
         * @return true if this match meets the minimum score (determined by the matching weights)
         */
        public boolean meetsMinimumScore() {
            double normalizedScore = ((MAX_POSSIBLE_SCORE / 2.0)* this.matchingWeights.minimumScore());
            return this.score >= normalizedScore;
        }
        
        /**
         * Compute the match score between two properties, broken up into arrays of
         * words at each property divider level.
         * 
         * @param aWords
         * @param bWords
         * @return
         */
        double computeWordMatchScore(List<List<String>> aWords, List<List<String>> bWords) {
            
            Set<String> aWordsRemaining = new LinkedHashSet<String>(flatten(aWords));
            Set<String> bWordsRemaining = new LinkedHashSet<String>(flatten(bWords));
            
            PriorityQueue<WordPair> orderedPairs = new PriorityQueue<WordPair>();
            double aDepth = 0;
            for (List<String> aWordList : aWords) {
                ++aDepth;
                for (String aWord : aWordList) {
                    double bDepth = 0;
                    for (List<String> bWordList: bWords) {
                        for (String bWord : bWordList) {
                            ++bDepth;
                            orderedPairs.add(new WordPair(aWord, bWord, (aDepth/aWords.size()), (bDepth/bWords.size()), matchingWeights));
                        }
                    } 
                }
            }
            
            double score = 0.0d;
            for (WordPair w: orderedPairs) {
                if (aWordsRemaining.contains(w.aWord) && bWordsRemaining.contains(w.bWord)) {
                    score += w.score;
                    aWordsRemaining.remove(w.aWord);
                    bWordsRemaining.remove(w.bWord);
                } 
            }
            
            double remains = (aWordsRemaining.size() + bWordsRemaining.size()) / 2.0;
            double initial = (aWords.size() + bWords.size()) / 2.0; 
            double unmatchedWordsCount = (remains - initial) * (matchingWeights.unmatchedWords());
            
            return score + unmatchedWordsCount;
        }
        
        private void computeOverallScore() {
            
            this.containsScore = this.matchingWeights.containsName() * (this.containsIgnoreCase ? 10 : 0);
            if (this.commonWordCount == 0) {
                this.commonWordsScore = 0.0;
            } else {
                this.commonWordsScore = (this.matchingWeights.commonWordCount()) * (Math.pow(2 * this.commonWordCount, 2.0)*((avgWordCount + commonWordCount)/avgWordCount));
            }
            this.typeMatchScore = (this.matchingWeights.typeMatch()) * this.typeMatch;
            this.score =  this.wordMatchScore + commonWordsScore + containsScore + typeMatchScore;
        }
        
        /**
         * WordPair is used to rank a match of a given set of words based on 
         * word depth and levenshtein distance between the words
         * 
         */
        private static class WordPair implements Comparable<WordPair>{
            private String aWord;
            private String bWord;
            private double score;
            
            private WordPair(String aWord, String bWord,  double aWordDepth, double bWordDepth, PropertyMatchingWeights matchingWeights) {
                this.aWord = aWord;
                this.bWord = bWord;
                double aDepth = (1.0 + aWordDepth) * (matchingWeights.nestedDepth);
                double bDepth = (1.0 + bWordDepth) * (matchingWeights.nestedDepth);
                double editDistance = getLevenshteinDistance(aWord, bWord);
                double distanceWeight =  matchingWeights.editDistance * (1.0 / (editDistance + 1.0));
                double wordLength = Math.max(aWord.length(), bWord.length());
                double wordLengthWeight = matchingWeights.editDistance * Math.sqrt(wordLength);
                this.score =  aDepth + bDepth + distanceWeight + wordLengthWeight;
            }
            /* (non-Javadoc)
             * @see java.lang.Comparable#compareTo(java.lang.Object)
             */
            public int compareTo(WordPair o) {
                double score = this.score - o.score;
                if (score < 0) {
                    return 1;
                } else if (score > 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
            
            public String toString() {
                return "[" + aWord + "],[" + bWord + "] = " + score;
            }
            /* (non-Javadoc)
             * @see java.lang.Object#hashCode()
             */
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((aWord == null) ? 0 : aWord.hashCode());
                result = prime * result + ((bWord == null) ? 0 : bWord.hashCode());
                long temp;
                temp = Double.doubleToLongBits(score);
                result = prime * result + (int) (temp ^ (temp >>> 32));
                return result;
            }
            /* (non-Javadoc)
             * @see java.lang.Object#equals(java.lang.Object)
             */
            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                WordPair other = (WordPair) obj;
                if (aWord == null) {
                    if (other.aWord != null)
                        return false;
                } else if (!aWord.equals(other.aWord))
                    return false;
                if (bWord == null) {
                    if (other.bWord != null)
                        return false;
                } else if (!bWord.equals(other.bWord))
                    return false;
                if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
                    return false;
                return true;
            }
            
            
        }
        
        /**
         * Computes the levenshtein distance of 2 strings
         * 
         * @param s
         * @param t
         * @return
         */
        private static int getLevenshteinDistance(String s, String t) {
            if (s == null || t == null) {
                throw new IllegalArgumentException("Strings must not be null");
            }
            int lengthOfS = s.length();
            int lengthOfT = t.length();
            
            if (lengthOfS == 0) {
                return lengthOfT;
            } else if (lengthOfT == 0) {
                return lengthOfS;
            }
            
            if (lengthOfS > lengthOfT) {
                // swap the input strings to consume less memory
                String tmp = s;
                s = t;
                t = tmp;
                lengthOfS = lengthOfT;
                lengthOfT = t.length();
            }
            
            int previousCosts[] = new int[lengthOfS + 1];
            int costs[] = new int[lengthOfS + 1];
            int swap[];
            
            int indexOfS;
            int indexOfT;
            
            char charAtIndexOfT; // jth character of t
            int cost;
            
            for (indexOfS = 0; indexOfS <= lengthOfS; indexOfS++) {
                previousCosts[indexOfS] = indexOfS;
            }
            
            for (indexOfT = 1; indexOfT <= lengthOfT; indexOfT++) {
                charAtIndexOfT = t.charAt(indexOfT - 1);
                costs[0] = indexOfT;
                
                for (indexOfS = 1; indexOfS <= lengthOfS; indexOfS++) {
                    cost = s.charAt(indexOfS - 1) == charAtIndexOfT ? 0 : 1;
                    // minimum of cell to the left+1, to the top+1, diagonally
                    // left and up +cost
                    costs[indexOfS] = Math.min(Math.min(costs[indexOfS - 1] + 1, previousCosts[indexOfS] + 1), previousCosts[indexOfS - 1]
                            + cost);
                }
                
                // copy current distance counts to 'previous row' distance
                // counts
                swap = previousCosts;
                previousCosts = costs;
                costs = swap;
            }
            
            // previousCosts now has the most recent cost counts
            return previousCosts[lengthOfS];
        }
        
        /**
         * Pattern is used to split a string into words on camel-case word boundaries
         */
        private static final String WORD_SPLITTER = String.format("%s|%s|%s|%s", 
                "([\\{\\}\\]\\[-_])", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])");
        
        /**
         * Splits a given property expression into arrays of lower-case words;
         * result is returned as a set of String[], which represent a property
         * component split on word boundaries.
         * 
         * @param s
         * @return
         */
        private static List<List<String>> splitIntoLowerCaseWords(String s) {
            List<List<String>> results = new ArrayList<List<String>>();
            for (String property: s.split("[.]")) {
                List<String> words = new LinkedList<String>();
                results.add(words);
                for (String word : property.split(WORD_SPLITTER)) {
                    if (word != null && word.trim().length() > 0) {
                        words.add(word.toLowerCase());
                    }
                }
            }
            return results;
        }
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(FieldMatchScore that) {
            /*
             * Higher scores are better, and should be ordered first ("lower")
             */
            if (this.score < that.score) {
                return 1;
            } else if (this.score > that.score) {
                return -1;
            } else {
                return 0;
            }
        }
        
        private int computeHashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((propertyA == null) ? 0 : propertyA.hashCode());
            result = prime * result + ((propertyB == null) ? 0 : propertyB.hashCode());
            return result;
        }
        
        public int hashCode() {
            return hashCode;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FieldMatchScore other = (FieldMatchScore) obj;
            if (propertyA == null) {
                if (other.propertyA != null)
                    return false;
            } else if (!propertyA.equals(other.propertyA))
                return false;
            if (propertyB == null) {
                if (other.propertyB != null)
                    return false;
            } else if (!propertyB.equals(other.propertyB))
                return false;
            return true;
        }
        
    }
    
}
