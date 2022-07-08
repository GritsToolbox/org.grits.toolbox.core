package org.grits.toolbox.core.typeahead;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;

public class PatriciaTrieContentProposalProvider extends
		SimpleContentProposalProvider {
	
	PatriciaTrie<String> trie;

	public PatriciaTrieContentProposalProvider(PatriciaTrie<String> trie) {
		super(null);
		this.trie = trie;
	}
	
	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		if (trie == null) 
			return null;
		// find the exact match if exists and put it as the first proposal
		Entry<String, String> entry = trie.select(contents.toLowerCase());
		SortedMap<String, String> resultMap = trie.prefixMap(contents.toLowerCase());
		IContentProposal[] result;
		int i=0;
		if (entry != null && !resultMap.containsValue(entry.getValue())) {
			result = new ContentProposal[resultMap.size()+1];
			result[i++] = new ContentProposal(entry.getValue(), entry.getKey(), null);
		}
		else 
			result = new ContentProposal[resultMap.size()];
		
		for (Iterator<Entry<String, String>> iterator = resultMap.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> match = iterator.next();
			result[i++] = new ContentProposal(match.getValue(), match.getKey(), null);
		}
		
		return result;
	}
	
}
