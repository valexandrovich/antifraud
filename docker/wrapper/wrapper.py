from configparser import ConfigParser
from datetime import datetime
from http.server import HTTPServer, BaseHTTPRequestHandler
from operator import truediv
import urllib
from urllib.request import urlopen
from urllib.parse import urlparse, parse_qs
import pathlib
from os.path import exists, getsize
import os
import json
import ssl
import sys


contentTypes = {
    'zip': 'application/zip',
    'xlsx': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'xls': 'application/vnd.ms-excel',
    'csv': 'text/csv',
    'json': 'application/json',
    'xml': 'text/xml'
}

def normalizePath(s, base):
    if (s):
        s = s.replace('\\', '/')
        # if s.endswith('$$'):
        #    s = s[:len(s) - 2]
        #    base = True

        if not s.endswith('/'):
            s += '/'
    else:
        s = ''
    if (not base):
        s += 'wrapper/'
    return s

def init():
    global cfg, revisionDate, packages, files, direct_links, address, domain, absolute, fullDomain, port, main_package_url, main_resource_url, https, proxies, url_path, path, env_var, env_var_append_wrapper
    cfg = ConfigParser(allow_no_value=True)
    cfg.read('config.ini')
    handleArguments()
    address = cfg.get('server', 'address')
    domain = cfg.get('server', 'domain')
    absolute = cfg.getboolean('server', 'absolute', fallback=False)
    port = cfg.getint('server', 'port')
    main_package_url = cfg.get('server', 'main_package_url')
    main_resource_url = cfg.get('server', 'main_resource_url')
    https = cfg.getboolean('server', 'https')
    revisionDate = datetime.fromisoformat(cfg.get('server', 'revisionDate'))
    url_path = cfg.get("server", "url_path", fallback="/api/3/action/download?file=")
    path = cfg.get('server', 'path', fallback='')
    env_var = cfg.get('server', 'env_var', fallback='')
    env_var_append_wrapper = cfg.getboolean('server', 'env_var_append_wrapper', fallback=True)

    if not path:
        if env_var:
            try:
                path = os.environ[env_var]
            except Exception as e:
                path = ""

            if (path):
                path = normalizePath(path, not env_var_append_wrapper)
        else:
            path = ""
    else:
        path = normalizePath(path, True)

    packages = dict(cfg.items('packages'))
    files = dict(cfg.items('files'))
    proxies = dict(cfg.items('proxies'))
    direct_links = dict(cfg.items('direct-links'))

    if proxies:
        proxy_support = urllib.request.ProxyHandler(proxies)
        opener = urllib.request.build_opener(proxy_support)
        urllib.request.install_opener(opener)

    if absolute:
        fullDomain = domain
    else:
        fullDomain = ("https://" if https else "http://") + domain + ":" + str(port)

    if fullDomain.endswith("/"):
        fullDomain = fullDomain[:len(fullDomain) - 1]

    if not url_path.startswith("/"):
        url_path = "/" + url_path

def setCommandLineArgument(s):
    dotPosition = s.find('.')
    if dotPosition >= 0:
       section = s[:dotPosition]
       s = s[dotPosition + 1:]
       eqPosition = s.find('=')
       if eqPosition >= 0:
           name = s[:eqPosition]
           value = s[eqPosition + 1:]
           cfg.set(section, name, value)

def handleArguments():
    for _, arg in enumerate(sys.argv[1:]):
        setCommandLineArgument(arg)

def getFormat(name):
    name=name.lower()
    suffix = pathlib.Path(name).suffix
    format = suffix[1:]
    return (name[0:-len(suffix)], format, contentTypes[format])

def copy(source, dest):
    try:
        while True:
            data = source.read(8192)
            if not data:
                break
            else:
                dest.write(data)
        return
    except Exception:
        pass

def tryRequest(url):
    try:
        r = urlopen(url)
        return r
    except Exception as e:
        return None

def getUrlFileSize(url):
    res = None
    try:
        u = urlopen(url)
        if u:
            res = u.headers['Content-length']
            u.close()

    except Exception as e:
        pass
    return res


class GovUaProxyRequestHandler(BaseHTTPRequestHandler):

    def sendJson(self, obj):
        data = json.dumps(obj, ensure_ascii=False, indent=4).encode('utf-8')
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Content-Length', memoryview(data).nbytes)
        self.end_headers()
        self.wfile.write(data)

    def makePackageResource(self, resourceName, resourceId)->object:
        fileName = files[resourceId].lower()
        name, format, mimetype = getFormat(fileName)

        if not resourceName:
            resourceName = name

        return {
            "name": resourceName,
            "id": resourceId,
            "format": format,
            "mimetype": mimetype
        }

    def handlePackage(self, query):
        if 'id' in query:
            id = query.get('id')[0]
        else:
            return False

        r = tryRequest(main_package_url.format_map({"id": id}))

        if not r:
            if not id in packages:
                return False

            resList = []
            resourceId = packages[id].lower()

            if resourceId.startswith('@'):
                items = dict(cfg.items(resourceId[1:]))
                for i, (key, value) in enumerate(items.items()):
                    resList.append(self.makePackageResource(key, value))
            else:
                resList.append(self.makePackageResource(None, resourceId))

            self.sendJson({
                "result": {
                    "resources": resList
                }
            })
        else:
            self.sendJson(json.loads(r.read().decode('utf-8')))

        return True

    def handleResource(self, query):
        if 'id' in query:
            id = query.get('id')[0]
        else:
            return False

        r = tryRequest(main_resource_url.format_map({"id": id}))

        if not r:
            if not id in files:
                return False

            fileName = files[id].lower()
            _, format, mimetype = getFormat(fileName)

            if id in direct_links and direct_links[id]:
                url = direct_links[id]
                fileSize = getUrlFileSize(url)
                if not fileSize:
                    return False
            else:
                fileSize = getsize(path + 'files/' + fileName)
                url = (fullDomain + url_path).format_map({"file": fileName})

            self.sendJson({
                "result": {
                    "resource_revisions": [
                        {
                            "url": url,
                            "size": fileSize,
                            "format": format,
                            "mimetype": mimetype,
                            "resource_created": revisionDate.isoformat()
                        }
                    ]
                }
            })
        else:
            self.sendJson(json.loads(r.read().decode('utf-8')))

        return True

    def handleDownload(self, query):
        if 'file' in query:
            file = query.get('file')[0]
            filePath = path + 'files/' + file;
            file = pathlib.Path(file).name
            if not exists(filePath):
                return False
        else:
            return False

        size = getsize(filePath)
        _, _, mimetype = getFormat(file)
        self.send_response(200)
        self.send_header('Content-type', mimetype)
        self.send_header('Content-Length', size)
        self.send_header('Content-Disposition', 'attachment; filename="' + file + '"')
        self.end_headers()
        with open(filePath, 'rb') as f:
            copy(f, self.wfile)
        return True

    def do_GET(self):
        print('Request received: ', self.path, flush = True)
        urlData = urlparse(self.path.lower())
        query = parse_qs(urlData.query)

        handled = False
        if urlData.path == '/api/3/action/package_show':
            handled = self.handlePackage(query)
        elif urlData.path == '/api/3/action/resource_show':
            handled = self.handleResource(query)
        elif urlData.path == '/api/3/action/download':
            handled = self.handleDownload(query)

        if not handled:
            self.send_response(404)
            self.end_headers()

def runApp():
    init()
    print('Data.gov.ua wrapper v.1.0', flush = True)
    print('Source folder:', path if path else '.', flush = True)

    httpd = HTTPServer((address, port), GovUaProxyRequestHandler)

    if https:
        print('https - is set', flush = True)
        httpd.socket = ssl.wrap_socket (httpd.socket,
            keyfile = path + '/ssl/key.pem',
            certfile = path + '/ssl/cert.pem', server_side = True)
    try:
        print('Downloading file URL prefix:', fullDomain, flush = True)
        print('Server on port', port, 'started...', flush = True)
        httpd.serve_forever()
    except KeyboardInterrupt:
        httpd.server_close()

if __name__ == '__main__':
    runApp()