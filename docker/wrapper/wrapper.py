import socket
from configparser import ConfigParser
from datetime import datetime
from http.client import HTTPException
from http.server import HTTPServer, BaseHTTPRequestHandler
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

DEBUG = "Debug |"


def normalize_path(s, base):
    if s:
        s = s.replace('\\', '/')

        if not s.endswith('/'):
            s += '/'
    else:
        s = ''
    if not base:
        s += 'wrapper/'
    return s


def init():
    global cfg, revisionDate, packages, files, direct_links, address, domain, absolute, full_domain, port,\
        main_package_url, main_resource_url, https, proxies, url_path, path, env_var, env_var_append_wrapper, debug
    cfg = ConfigParser(allow_no_value=True)
    cfg.read('config.ini')
    handle_arguments()
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
    debug = cfg.getboolean('server', 'debug', fallback=False)

    if not path:
        if env_var:
            try:
                path = os.environ[env_var]
            except Exception:
                path = ""

            if path:
                path = normalize_path(path, not env_var_append_wrapper)
        else:
            path = ""
    else:
        path = normalize_path(path, True)

    packages = dict(cfg.items('packages'))
    files = dict(cfg.items('files'))
    proxies = dict(cfg.items('proxies'))
    direct_links = dict(cfg.items('direct-links'))

    if proxies:
        proxy_support = urllib.request.ProxyHandler(proxies)
        opener = urllib.request.build_opener(proxy_support)
        urllib.request.install_opener(opener)

    if absolute:
        full_domain = domain
    else:
        full_domain = ("https://" if https else "http://") + domain + ":" + str(port)

    if full_domain.endswith("/"):
        full_domain = full_domain[:len(full_domain) - 1]

    if not url_path.startswith("/"):
        url_path = "/" + url_path

    if debug:
        print(f"{DEBUG} Wrapper service initialized with:")
        print(f"{DEBUG} Path: {path}")
        print(f"{DEBUG} Packages: {packages}")
        print(f"{DEBUG} Files: {files}")
        print(f"{DEBUG} Proxies: {proxies}")
        print(f"{DEBUG} Direct Links: {direct_links}")
        print(f"{DEBUG} Full Domain: {full_domain}")
        print(f"{DEBUG} URL path: {url_path}")


def set_command_line_argument(s):
    dot_position = s.find('.')
    if dot_position >= 0:
        section = s[:dot_position]
        s = s[dot_position + 1:]
        eq_position = s.find('=')
        if eq_position >= 0:
            name = s[:eq_position]
            value = s[eq_position + 1:]
            cfg.set(section, name, value)


def handle_arguments():
    for _, arg in enumerate(sys.argv[1:]):
        set_command_line_argument(arg)


def get_format(name):
    name = name.lower()
    suffix = pathlib.Path(name).suffix
    file_format = suffix[1:]
    return name[0:-len(suffix)], file_format, contentTypes[file_format]


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


def try_request(url):
    try:
        r = urlopen(url)
        return r
    except urllib.error.HTTPError as e:
        print(f"{DEBUG} + HTTPError: {e.code}, Reason: {e.reason}")
    except urllib.error.URLError as e:
        print(f"{DEBUG} + URLError: {e.reason}")
    except (HTTPException, socket.error) as e:
        print(f"{DEBUG} + HTTP/Socket Exception: {e}")
    except Exception:
        import traceback
        print(f"{DEBUG} + Generic Exception: {traceback.format_exc()}")
    return None


def get_url_file_size(url):
    res = None
    try:
        u = urlopen(url)
        if u:
            res = u.headers['Content-length']
            u.close()

    except Exception:
        pass
    return res


class GovUaProxyRequestHandler(BaseHTTPRequestHandler):

    def send_json(self, obj):
        if debug:
            print(f"{DEBUG} sendJson: {obj}")
        data = json.dumps(obj, ensure_ascii=False, indent=4).encode('utf-8')
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Content-Length', str(memoryview(data).nbytes))
        self.end_headers()
        self.wfile.write(data)

    @staticmethod
    def make_package_resource(resource_name, resource_id) -> object:
        if debug:
            print(f"{DEBUG} makePackageResource: {resource_name}; id: {resource_id}")
        file_name = files[resource_id].lower()
        name, file_format, mimetype = get_format(file_name)

        if not resource_name:
            resource_name = name

        return {
            "name": resource_name,
            "id": resource_id,
            "format": file_format,
            "mimetype": mimetype
        }

    def handle_package(self, query):
        if debug:
            print(f"{DEBUG} handlePackage: {query}")
        if 'id' in query:
            query_id = query.get('id')[0]
        else:
            return False

        r = try_request(main_package_url.format_map({"id": query_id}))

        if not r:
            if query_id not in packages:
                return False

            res_list = []
            resource_id = packages[query_id].lower()

            if resource_id.startswith('@'):
                items = dict(cfg.items(resource_id[1:]))
                for i, (key, value) in enumerate(items.items()):
                    res_list.append(self.make_package_resource(key, value))
            else:
                res_list.append(self.make_package_resource(None, resource_id))

            self.send_json({
                "result": {
                    "resources": res_list
                }
            })
        else:
            self.send_json(json.loads(r.read().decode('utf-8')))

        return True

    def handle_resource(self, query):
        if debug:
            print(f"{DEBUG} handleResource: {query}")
        if 'id' in query:
            query_id = query.get('id')[0]
        else:
            return False

        r = try_request(main_resource_url.format_map({"id": query_id}))

        if not r:
            if debug:
                print(f"{DEBUG} * Request was not successful to: " + main_resource_url.format_map({"id": query_id}))
            if query_id not in files:
                if debug:
                    print(f"{DEBUG} * Id is not in files")
                return False

            file_name = files[query_id].lower()
            _, file_format, mimetype = get_format(file_name)

            if query_id in direct_links and direct_links[query_id]:
                url = direct_links[query_id]
                if debug:
                    print(f"{DEBUG} * Getting data from {url}")
                file_size = get_url_file_size(url)
                if not file_size:
                    if debug:
                        print(f"{DEBUG} * Getting data was not successful")
                    return False
            else:
                file_size = getsize(path + 'files/' + file_name)
                url = (full_domain + url_path).format_map({"file": file_name})
                if debug:
                    print(f"{DEBUG} * Using preloaded data from {url}")

            self.send_json({
                "result": {
                    "resource_revisions": [
                        {
                            "url": url,
                            "size": file_size,
                            "format": file_format,
                            "mimetype": mimetype,
                            "resource_created": revisionDate.isoformat()
                        }
                    ]
                }
            })
        else:
            self.send_json(json.loads(r.read().decode('utf-8')))

        return True

    def handle_download(self, query):
        if debug:
            print(f"{DEBUG} handleDownload: {query}")
        if 'file' in query:
            query_file = query.get('file')[0]
            file_path = path + 'files/' + query_file
            query_file = pathlib.Path(query_file).name
            if debug:
                print(f"{DEBUG} * Using file path: {file_path}")
            if not exists(file_path):
                if debug:
                    print(f"{DEBUG} * File path does not exist")
                return False
        else:
            if debug:
                print(f"{DEBUG} * File was not mentioned in query")
            return False

        size = getsize(file_path)
        _, _, mimetype = get_format(query_file)
        self.send_response(200)
        self.send_header('Content-type', mimetype)
        self.send_header('Content-Length', str(size))
        self.send_header('Content-Disposition', 'attachment; filename="' + query_file + '"')
        self.end_headers()
        with open(file_path, 'rb') as f:
            copy(f, self.wfile)
            if debug:
                print(f"{DEBUG} * File copied")
        return True

    def do_GET(self):
        if debug:
            print(f"{DEBUG} do_GET")
        print('Request received: ', self.path, flush=True)
        url_data = urlparse(self.path.lower())
        query = parse_qs(url_data.query)

        handled = False
        if url_data.path == '/api/3/action/package_show':
            handled = self.handle_package(query)
        elif url_data.path == '/api/3/action/resource_show':
            handled = self.handle_resource(query)
        elif url_data.path == '/api/3/action/download':
            handled = self.handle_download(query)

        if not handled:
            self.send_response(404)
            self.end_headers()


def run_app():
    init()
    print('Data.gov.ua wrapper v.5.2.312', flush=True)
    print('Source folder:', path if path else '.', flush=True)

    httpd = HTTPServer((address, port), GovUaProxyRequestHandler)

    if https:
        print('https - is set', flush=True)
        httpd.socket = ssl.wrap_socket(httpd.socket,
                                       keyfile=path + '/ssl/key.pem',
                                       certfile=path + '/ssl/cert.pem', server_side=True)
    try:
        print('Downloading file URL prefix:', full_domain, flush=True)
        print('Server on port', port, 'started...', flush=True)
        httpd.serve_forever()
    except KeyboardInterrupt:
        httpd.server_close()


if __name__ == '__main__':
    run_app()
