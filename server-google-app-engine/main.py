import webapp2
import jinja2
import os
import json
from google.appengine.ext import ndb

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)

class BaseHandler(webapp2.RequestHandler):
    def render(self,html,values={}):
        template = JINJA_ENVIRONMENT.get_template(html)
        self.response.write(template.render(values))

class LogData(ndb.Model):
    name = ndb.StringProperty()
    date = ndb.DateTimeProperty(auto_now_add=True)

class MainHandler(webapp2.RequestHandler):
    def get(self):
        param = self.request.get('msg')
        self.response.write('{"message":'+'"'+param+'"'+'}')

    def post(self):
        name = self.request.get('name')

        if name is None:
            self.redirect('/?msg='+'error')

        log_data = LogData()
        log_data.name = name;
        log_data.put()
        self.redirect('/?msg='+'success')

class LogHandler(BaseHandler):
    def get(self):
        logs = LogData.query().order(-LogData.date).fetch(1000)
        values = {
            'logs':logs
        }
        self.render('main.html', values)

class UuidMethodHandler(BaseHandler):
    def get(self):
        f = open('config.json', 'r')
        jsonData = json.load(f)
        self.response.write(json.dumps(jsonData, sort_keys = True, indent = 4))
        f.close()

app = webapp2.WSGIApplication([
    ('/log',LogHandler),
    ('/config',UuidMethodHandler),
    ('/', MainHandler),
    ('/(.*?)', MainHandler)
], debug=True)
