/*
 * Copyright (c) 2014-2016 Jan Strauß <jan[at]over9000.eu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.over9000.skadi.service;

import eu.over9000.skadi.model.Channel;
import eu.over9000.skadi.model.StateContainer;
import eu.over9000.skadi.model.StreamQuality;
import eu.over9000.skadi.remote.StreamQualityRetriever;
import javafx.concurrent.Task;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QualityRetrievalService extends AbstractSkadiService<List<MenuItem>> {

	private final Consumer<StreamQuality> consumer;
	private final Channel channel;
	private final StateContainer state;

	public QualityRetrievalService(final Consumer<StreamQuality> consumer, final Channel channel, final StateContainer state) {
		this.consumer = consumer;
		this.channel = channel;
		this.state = state;
	}

	@Override
	protected Task<List<MenuItem>> createTask() {
		return new Task<List<MenuItem>>() {

			@Override
			protected List<MenuItem> call() throws Exception {

				final List<MenuItem> result = new ArrayList<>();

				StreamQualityRetriever.retrieveQualities(channel, state).forEach(quality -> {
					final MenuItem mi = new MenuItem("Stream: " + quality.getQuality());
					mi.setOnAction(event -> consumer.accept(quality));
					result.add(mi);
				});

				return result;
			}
		};
	}
}
