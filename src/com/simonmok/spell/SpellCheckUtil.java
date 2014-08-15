package com.simonmok.spell;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import net.sf.json.JSONObject;

public abstract class SpellCheckUtil {

	private static final String DICTIONARY_FILENAME = "dictionary.txt";
	
	public static final int MAX_SUGGESTION = 10;
	public static enum SearchOption {
		FULL_DICTIONARY,
		WITH_FIRST_CHARACTER_MATCH
	}
	
	private static List<String> dictionary;
	
	static {
		try {
			final Thread thread = Thread.currentThread();
			final ClassLoader loader = thread.getContextClassLoader();
			final URL url = loader.getResource(DICTIONARY_FILENAME);
			final File file = new File(url.toURI());
			final FileInputStream fileInputStream = new FileInputStream(file);
			final DataInputStream dataInputStream = new DataInputStream(fileInputStream);
			final InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
			final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			dictionary = new ArrayList<String>();
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				dictionary.add(line);
			}
			
			bufferedReader.close();
			inputStreamReader.close();
			dataInputStream.close();
			fileInputStream.close();
		} catch (final URISyntaxException e) {
			System.err.println("Fail to get resource for dictionary file");
		} catch (final IOException e) {
			System.err.println("Error in reading dictionary file");
		}		
	}
	
	public static final boolean isSpellCheckable(final String word) {
		
		final Pattern includePattern = Pattern.compile("^[a-zA-Z'-]+$");
		final Matcher includeMatcher = includePattern.matcher(word);
		final Pattern excludePattern = Pattern.compile("^[A-Z'-]+$");
		final Matcher excludeMatcher = excludePattern.matcher(word);
		return includeMatcher.find() && !excludeMatcher.find();
	}
	
	public static final List<String> splitPassage(final String passage) {
		
		if (passage == null) {
			return new ArrayList<String>();
		}
		
		final String[] array = passage.split("[^a-zA-Z'-]");
		return Arrays.asList(array);
	}
	
	public static final boolean spellCheck(final String word) {
		
		final Map<String, Integer> map = getDistance(word, SearchOption.WITH_FIRST_CHARACTER_MATCH);
		return map.size() == 1;
	}
	
	public static final List<String> suggest(final String word, final SearchOption searchOption) {
		
		final Map<String, Integer> map = getDistance(word, searchOption);
		final DistanceComparator comparator =  new DistanceComparator(map);
		final TreeMap<String, Integer> sortedMap = new TreeMap<String, Integer>(comparator);
        sortedMap.putAll(map);
        
        final List<String> results = new ArrayList<String>(sortedMap.keySet());
        return results.size() > MAX_SUGGESTION ? results.subList(0, MAX_SUGGESTION) : results;
	}
	
	private static final Map<String, Integer> getDistance(final String word, final SearchOption searchOption) {
		
		final Map<String, Integer> map = new HashMap<String, Integer>();
		
		if (word != null &&  word.length() > 1) {
			final String upperWord = word.toUpperCase();
			final String startChar = upperWord.substring(0, 1);
			
			for (final String dictionaryWord : dictionary) {
				String upperDictionaryWord = dictionaryWord.toUpperCase();
				
				if (searchOption == SearchOption.WITH_FIRST_CHARACTER_MATCH) {
					if (upperDictionaryWord.startsWith(startChar)) {
						final int distance = levenshteinDistance(upperWord, upperDictionaryWord);
						if (distance == 0) {
							map.clear();
							return map;
						} else {
							map.put(dictionaryWord, distance);
						}
					} else {
						if (map.size() > 0) {
							break;
						}
					}
				}
				
				if (searchOption == SearchOption.FULL_DICTIONARY) {
					final int distance = levenshteinDistance(upperWord, upperDictionaryWord);
					if (distance == 0) {
						map.clear();
						return map;
					} else {
						map.put(dictionaryWord, distance);
					}
				}
			}
		}
		return map;
	}
 
	private static final int levenshteinDistance(final CharSequence str1, final CharSequence str2) {
		
		final int[][] distance = new int[str1.length() + 1][str2.length() + 1];
 
		for (int i = 0; i <= str1.length(); i++) {
			distance[i][0] = i;
		}
		for (int j = 0; j <= str2.length(); j++) {
			distance[0][j] = j;
		}
 
		for (int i = 1; i <= str1.length(); i++) {
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = Math.min(
					Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1),
					distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));
		}
		return distance[str1.length()][str2.length()];
    }
	
	public static final Map<String, List<String>> suggestForPassage(final String passage, final SearchOption searchOption) {
		
		final List<String> words = splitPassage(passage);
		final Map<String, List<String>> result = new TreeMap<String, List<String>>();
		for (final String word : words) {
			if (!word.equals("")) {
				if (isSpellCheckable(word)) {
					final String upperWord = word.toUpperCase();
					if (!result.containsKey(upperWord)) {
						final List<String> suggest = suggest(word, searchOption);
						if (suggest.size() > 0) {
							result.put(upperWord, suggest);
						}
					}
				}
			}
		}
		return result;
	}
	
	public static String getJsonSuggestion(final String passage, final SearchOption searchOption) {
		
		final Map<String, List<String>> map = suggestForPassage(passage, SearchOption.FULL_DICTIONARY);
		final StringBuilder builder = new StringBuilder();
		builder.append("{");
		
		for (final String key : map.keySet()) {
			if (builder.length() > 1) {
				builder.append(",");
			}
			final List<String> list = map.get(key);
			builder.append("\"");
			builder.append(key);
			builder.append("\":[\"");
			builder.append(stringJoin(list, "\",\""));
			builder.append("\"]");
		}
		
		builder.append("}");
		return builder.toString();
	}
	
	private static final String stringJoin(final Collection<String> string, final String delimiter) {
		
		final StringBuilder builder = new StringBuilder();
		final Iterator<String> iter = string.iterator();
		
		while (iter.hasNext()) {
			builder.append(iter.next());
			if (!iter.hasNext()) {
				break;
			}
			
			builder.append(delimiter);
		}
		
		return builder.toString();
	}
	
	/*public static final String getJsonSuggestion(final String passage, final SearchOption searchOption) {
	 * 
		final Map<String, List<String>> map = suggestForPassage(passage, SearchOption.FULL_DICTIONARY);
		final JSONObject jsonObject = JSONObject.fromObject(map);
		return jsonObject.toString();
	}*/	
}