grammar TorpedodepotEmail;

/**
 * PARSER RULES
 * The main rule is 'emailAddress', which must follow the pattern:
 * <local-part> @ torpedodepot . <domain-suffix>
 */

emailAddress
    : localPart '@' 'torpedodepot' '.' domainSuffix
    ;

localPart
    // Must be one or more ALPHANUM characters.
    // Enforces the rule of no special characters.
    : ALPHANUM+
    ;

domainSuffix
    // The allowed domain suffixes.
    : 'com'
    | 'net'
    | 'wannabemil'
    ;


/**
 * LEXER RULES
 */

// Fragments are reusable building blocks for other lexer rules, they don't become tokens themselves.
fragment ALPHA: ('a'..'z' | 'A'..'Z');
fragment DIGIT: ('0'..'9');

// This token captures any single character that is either a letter or a digit.
ALPHANUM
    : ALPHA | DIGIT
    ;
