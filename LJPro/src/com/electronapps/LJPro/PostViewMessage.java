	/***
	Copyright (c) 2008-2009 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.electronapps.LJPro;




import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.Editable;
import android.text.style.ImageSpan;
import android.widget.ImageView;
import android.widget.TextView;

public class PostViewMessage {
	private String key;
	private String Id;
	private int layerId;
	private LayerDrawable frame;
	private Editable editable;
	private String url;
	private ImageSpan span;
	
	public PostViewMessage(String key) {
		this.key=key;
	}
	
	public String getKey() {
		return(key);
	}
	

	
	public Editable getEditable() {
		return(editable);
	}
	public void setLayerId(int id) {
		this.layerId=id;
	}
	
	public int getLayerId(){
		return layerId;
	}
	
	public void setFrame(LayerDrawable l) {
		this.frame=l;
	}
	
	public LayerDrawable getFrame() {
		return frame;
	}
	
	
	public void setEditable(Editable editable) {
		this.editable=editable;
	}
	
	public String getUrl() {
		return(url);
	}
	
	public void setUrl(String url) {
		this.url=url;
	}




	
	



	public void setSpan(ImageSpan span) {
		this.span=span;
		
	}
	


	
	public String getId() {
		return(this.Id);
		
	}

	

	public ImageSpan getSpan() {
		return(this.span);
		
	}

	public void setId(String itemid) {
		this.Id=itemid;
		
	}
}