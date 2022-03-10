import unittest

class TestStringMethods(unittest.TestCase):
    buffer = ""

    def write(self, text):
        self.buffer += text

    def test_upper(self):
        self.write('upper with "foo" => "' + 'foo'.upper() + '"')

    def test_isupper(self):
        self.write('isupper with "FOO" => "' + str('FOO'.isupper()) + '"\n\n')
        self.write('isupper with "Foo" => "' + str('Foo'.isupper()) + '"\n\n')

    def test_split(self):
        text = 'hello world'

        self.write("\nsplit without parameter\n\n")
        self.check_split_with(text)
        self.write("\nsplit with an int\n\n")
        self.check_split_with(text, 2)

    def check_split_with(self, text, value=None):
        self.write("====\n\n")
        try:
            if (value == None):
                splitted = text.split()
            else:
                splitted = text.split(value)

            self.write('"' + text + '".split(' + (str(value) if value!=None else "") + ') => "' + str(splitted) + '"\n\n')
        except TypeError:
            self.write('"' + text + '".split(' + (str(value) if value!=None else "") + ') => Exception\n\n')
        self.write("====\n\n")

    def tearDown(self):
        methodName=self._testMethodName
        with open("../docs/_" + methodName + ".adoc", "w") as f:
            f.write("= " + self._testMethodName + "\n\n")
            f.write(self.buffer)
            f.write("\n")


if __name__ == '__main__':
    unittest.main()
