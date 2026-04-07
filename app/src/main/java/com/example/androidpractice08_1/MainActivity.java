package com.example.androidpractice08_1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

// SAX 相关导入
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;

// DOM 相关导入
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

// ★关键步骤：确保导入的是你自己包名的 R，而不是 android.R
import com.example.androidpractice08_1.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSax, btnDom, btnPull, btnWrite;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定控件
        btnSax = findViewById(R.id.btn_sax);
        btnDom = findViewById(R.id.btn_dom);
        btnPull = findViewById(R.id.btn_pull);
        btnWrite = findViewById(R.id.btn_write);
        tvResult = findViewById(R.id.tv_result);

        // 设置点击监听
        btnSax.setOnClickListener(this);
        btnDom.setOnClickListener(this);
        btnPull.setOnClickListener(this);
        btnWrite.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_sax) {
            parseWithSAX();
        } else if (v.getId() == R.id.btn_dom) {
            parseWithDOM();
        } else if (v.getId() == R.id.btn_pull) {
            parseWithPull();
        } else if (v.getId() == R.id.btn_write) {
            generateXML();
        }
    }

    // ==========================================
    // 1. SAX 解析
    // ==========================================
    private void parseWithSAX() {
        try {
            // 使用 openRawResource 读取 res/xml 下的文件
            InputStream is = getResources().openRawResource(R.xml.data);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader reader = factory.newSAXParser().getXMLReader();
            ContentHandler handler = new ContentHandler();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(is));

        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("SAX解析出错: " + e.getMessage());
        }
    }

    // SAX 的 Handler 内部类
    class ContentHandler extends DefaultHandler {
        private String currentTag;
        private String currentValue;
        private final List<String> names = new ArrayList<>();
        private final List<String> ages = new ArrayList<>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            currentTag = localName;
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            currentValue = new String(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if ("name".equals(localName)) {
                names.add(currentValue);
            } else if ("age".equals(localName)) {
                ages.add(currentValue);
            }
        }

        @Override
        public void endDocument() {
            // 解析结束后更新UI (注意：SAX通常在子线程，这里简化处理，实际开发需用Handler或runOnUiThread)
            runOnUiThread(() -> {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < names.size(); i++) {
                    sb.append("姓名：").append(names.get(i))
                            .append("，年龄：").append(ages.get(i)).append("\n\n");
                }
                tvResult.setText(sb.toString());
            });
        }
    }

    // ==========================================
    // 2. DOM 解析
    // ==========================================
    private void parseWithDOM() {
        try {
            InputStream is = getResources().openRawResource(R.xml.data);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            Element root = doc.getDocumentElement();
            NodeList nodeList = root.getElementsByTagName("person");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                    String age = element.getElementsByTagName("age").item(0).getChildNodes().item(0).getNodeValue();
                    sb.append("姓名：").append(name).append("，年龄：").append(age).append("\n\n");
                }
            }
            tvResult.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("DOM解析出错");
        }
    }

    // ==========================================
    // 3. PULL 解析
    // ==========================================
    private void parseWithPull() {
        try {
            InputStream is = getResources().openRawResource(R.xml.data);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, "utf-8");

            int eventType = parser.getEventType();
            String name = "";
            String age = "";
            StringBuilder sb = new StringBuilder();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("name".equals(tagName)) {
                            name = parser.nextText();
                        } else if ("age".equals(tagName)) {
                            age = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("person".equals(tagName)) {
                            sb.append("姓名：").append(name).append("，年龄：").append(age).append("\n\n");
                        }
                        break;
                }
                eventType = parser.next();
            }
            tvResult.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("PULL解析出错");
        }
    }

    // ==========================================
    // 4. 生成 XML
    // ==========================================
    private void generateXML() {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlSerializer serializer = factory.newSerializer();
            StringWriter writer = new StringWriter();
            serializer.setOutput(writer);

            serializer.startDocument("UTF-8", true);
            serializer.startTag(null, "persons");

            // 生成第一个人
            serializer.startTag(null, "person");
            serializer.attribute(null, "id", "1");

            serializer.startTag(null, "name");
            serializer.text("GeneratedUser");
            serializer.endTag(null, "name");

            serializer.startTag(null, "age");
            serializer.text("25");
            serializer.endTag(null, "age");

            serializer.endTag(null, "person");
            serializer.endTag(null, "persons");
            serializer.endDocument();

            tvResult.setText("生成的XML如下：\n" + writer.toString());

        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("生成XML出错");
        }
    }
}