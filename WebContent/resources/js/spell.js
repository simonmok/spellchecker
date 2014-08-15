function highlight() {
	var text = $('span#preview').text();
	for (var word in suggestions) {
		var regExp = new RegExp('\\b(' + word + ')\\b', 'gi');
		text = text.replace(regExp, '<span class="error">$1</span>');
	}
	$('span#preview').html(text);
}

function init() {
	var text = $('textarea').text();
	$('span#preview').text(text);
	
	var length = 0;
	var first = '';
	for (var word in suggestions) {
		if (first == '') {
			first = word;
		}
		if (suggestions[word].length > 0) {
			length++;
		}
	}
	
	if (length == 0) {
		if (text.length > 0) {
			$('table.spellTable').replaceWith('No spelling errors found.');
		}

	} else {
		total = length;
		cursor = 1;
		$('span#cursor').text(cursor);
		$('span#total').text(total);
		$('input#original').val(first);
		addOptions(first);
		highlight();
		$('table.spellTable').show();
	}
}

function addOptions(word) {
	var suggestList = suggestions[word];
	for (var i = 0; i < suggestList.length; i++) {
	     $('select#suggestion')
	         .append($("<option></option>")
	         .attr("value", suggestList[i])
	         .text(suggestList[i]));
	}
}

function change() {
	var original = $('input#original').val();
	var change = $('select#suggestion').val();
	if (change != null) {
		var updated = $('span#preview').text();
		var regExp = new RegExp('\\b' + original + '\\b', 'gi');
		
		updated = updated.replace(regExp, function (match) {
			var firstChar = match.charAt(0);
			var titleCase = firstChar == firstChar.toUpperCase();
			return titleCase ? toTitleCase(change) : change;
		});
		$('span#preview').text(updated);
		highlight();
		ignore();
	}
}

function ignore() {
	var current = $('input#original').val().toUpperCase();
	var next = '';
	var currentFound = false;
	
	for (var word in suggestions) {
		if (currentFound) {
			next = word.toUpperCase();
			break;
		} else {
			if (word == current) {
				currentFound = true;
			}
		}
	}
	
	if (next == '') {
		$('input#change').attr('disabled', 'disabled');
		$('input#ignore').attr('disabled', 'disabled');
		$('input#original').attr('disabled', 'disabled');
		$('select#suggestion').attr('disabled', 'disabled');
		alert('End of list reached');
	} else {
		$("select#suggestion option").each(function() {
		    $(this).remove();
		});
		
		$('input#original').val(next);
		addOptions(next);
		$('span#cursor').text(++cursor);
		$('span#total').text(total);
	}
}

function apply() {
	$('textarea').val($('span#preview').text());
}

function toTitleCase(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}