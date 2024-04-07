# Java Test ToolBelt

## FileDbImporter

Stores files in Database, which allows you to pack data from multiple tables into a single file. Use it to set up data before starting a test.

### Import File Format

1. [TableName]
2. {Field Labels}
3. Data

* Separator: ;


## FileDbComparator

Compares a file to a Database, which can describe data for multiple tables in a single file. It can verify the existence of data or the absence of data.

### Verify File Format

1. [TableName]
2. {Field Labels}　If you put @ at the end of a field name, that field can be excluded from the comparison.
3. Data

* Separator: ;
